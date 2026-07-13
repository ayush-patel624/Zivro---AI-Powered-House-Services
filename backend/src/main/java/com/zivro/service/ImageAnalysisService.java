package com.zivro.service;

import com.zivro.dto.ImageAnalysisResponse;
import com.zivro.exception.BadRequestException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ImageAnalysisService {

    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp", "image/gif");

    public ImageAnalysisResponse analyze(MultipartFile file, String serviceIconKey) {
        validate(file);
        try (InputStream in = file.getInputStream()) {
            BufferedImage image = ImageIO.read(in);
            if (image == null) {
                throw new BadRequestException("Could not read image for analysis.");
            }
            image = scaleForAnalysis(image);
            Features features = extractFeatures(image);
            String icon = serviceIconKey == null ? "full-cleaning" : serviceIconKey.toLowerCase(Locale.ROOT);
            return buildResult(icon, features);
        } catch (IOException e) {
            throw new BadRequestException("Could not analyze image: " + e.getMessage());
        }
    }

    public void applyToBookingImage(com.zivro.domain.BookingImage row, ImageAnalysisResponse analysis) {
        row.setAiDetectedType(analysis.getDetectedType());
        row.setAiLabel(analysis.getLabel());
        row.setAiQuantity(analysis.getQuantity());
        row.setAiQuantityUnit(analysis.getQuantityUnit());
        row.setAiEstimatedMinutes(analysis.getEstimatedMinutes());
        row.setAiStainLevel(analysis.getStainLevel());
        row.setAiConfidence(java.math.BigDecimal.valueOf(analysis.getConfidence()));
        row.setAiDetailsJson(analysis.getSummary());
    }

    public ImageAnalysisResponse fromBookingImage(com.zivro.domain.BookingImage row) {
        if (row == null || row.getAiDetectedType() == null) {
            return null;
        }
        return ImageAnalysisResponse.builder()
                .detectedType(row.getAiDetectedType())
                .label(row.getAiLabel())
                .quantity(row.getAiQuantity())
                .quantityUnit(row.getAiQuantityUnit())
                .estimatedMinutes(row.getAiEstimatedMinutes())
                .stainLevel(row.getAiStainLevel())
                .confidence(row.getAiConfidence() != null ? row.getAiConfidence().doubleValue() : null)
                .summary(row.getAiDetailsJson())
                .build();
    }

    private ImageAnalysisResponse buildResult(String iconKey, Features f) {
        Scene scene = classifyScene(iconKey, f);
        int stainMinutes = stainMinutes(f.stainLevel);
        return switch (scene) {
            case UTENSILS -> utensilsResult(f, stainMinutes);
            case DISHES -> dishesResult(f, stainMinutes);
            case ROOM -> roomResult(f, stainMinutes, "Room");
            case WASHROOM -> roomResult(f, stainMinutes, "Washroom");
            case APPLIANCE -> applianceResult(f, stainMinutes, iconKey);
            case VEHICLE -> vehicleResult(f, stainMinutes);
            case LAUNDRY -> laundryResult(f, stainMinutes);
            default -> roomResult(f, stainMinutes, "Service area");
        };
    }

    private ImageAnalysisResponse utensilsResult(Features f, int stainMinutes) {
        int count = clamp(6 + (int) (f.edgeDensity * 45) + f.blobCells / 3, 4, 80);
        int minutes = clamp(8 + count / 4 + stainMinutes, 5, 90);
        return ImageAnalysisResponse.builder()
                .detectedType("UTENSILS")
                .label("Utensils")
                .quantity(count)
                .quantityUnit("pieces")
                .estimatedMinutes(minutes)
                .stainLevel(f.stainLevel)
                .confidence(confidence(f, 0.78))
                .summary("Detected piled utensils (~" + count + " pieces). Estimated scrub time " + minutes + " min.")
                .build();
    }

    private ImageAnalysisResponse dishesResult(Features f, int stainMinutes) {
        int count = clamp(5 + (int) (f.edgeDensity * 35) + f.blobCells / 4, 3, 60);
        int minutes = clamp(6 + count / 5 + stainMinutes, 4, 75);
        return ImageAnalysisResponse.builder()
                .detectedType("DISHES")
                .label("Dishes")
                .quantity(count)
                .quantityUnit("items")
                .estimatedMinutes(minutes)
                .stainLevel(f.stainLevel)
                .confidence(confidence(f, 0.76))
                .summary("Detected dishes (~" + count + " items). Estimated wash time " + minutes + " min.")
                .build();
    }

    private ImageAnalysisResponse roomResult(Features f, int stainMinutes, String label) {
        int sqft = clamp(90 + (int) (f.megapixels * 38 * f.aspectRatio), 80, 650);
        int minutes = clamp(20 + sqft / 18 + stainMinutes, 15, 240);
        return ImageAnalysisResponse.builder()
                .detectedType("ROOM")
                .label(label)
                .quantity(sqft)
                .quantityUnit("sq ft")
                .estimatedMinutes(minutes)
                .stainLevel(f.stainLevel)
                .confidence(confidence(f, 0.81))
                .summary(label + " approx " + sqft + " sq ft. Estimated cleaning " + minutes + " min.")
                .build();
    }

    private ImageAnalysisResponse applianceResult(Features f, int stainMinutes, String iconKey) {
        String appliance = pickAppliance(f, iconKey);
        int minutes = switch (appliance) {
            case "Refrigerator" -> clamp(18 + stainMinutes, 12, 45);
            case "Washing machine" -> clamp(14 + stainMinutes, 10, 35);
            case "Television" -> clamp(5 + stainMinutes, 3, 15);
            case "Air conditioner" -> clamp(25 + stainMinutes, 20, 60);
            default -> clamp(12 + stainMinutes, 8, 40);
        };
        return ImageAnalysisResponse.builder()
                .detectedType("APPLIANCE")
                .label(appliance)
                .quantity(1)
                .quantityUnit("unit")
                .estimatedMinutes(minutes)
                .stainLevel(f.stainLevel)
                .confidence(confidence(f, 0.74))
                .summary("Detected " + appliance.toLowerCase(Locale.ROOT) + ". Estimated service time " + minutes + " min.")
                .build();
    }

    private ImageAnalysisResponse vehicleResult(Features f, int stainMinutes) {
        String size = f.aspectRatio > 1.35 ? "SUV / large vehicle" : "Car / compact vehicle";
        int minutes = clamp(25 + stainMinutes + (int) (f.megapixels * 4), 20, 90);
        return ImageAnalysisResponse.builder()
                .detectedType("VEHICLE")
                .label(size)
                .quantity(1)
                .quantityUnit("vehicle")
                .estimatedMinutes(minutes)
                .stainLevel(f.stainLevel)
                .confidence(confidence(f, 0.72))
                .summary(size + " detected. Estimated cleaning " + minutes + " min.")
                .build();
    }

    private ImageAnalysisResponse laundryResult(Features f, int stainMinutes) {
        int loads = clamp(1 + f.blobCells / 8, 1, 5);
        int minutes = clamp(loads * 20 + stainMinutes, 15, 120);
        return ImageAnalysisResponse.builder()
                .detectedType("LAUNDRY")
                .label("Laundry load")
                .quantity(loads)
                .quantityUnit("loads")
                .estimatedMinutes(minutes)
                .stainLevel(f.stainLevel)
                .confidence(confidence(f, 0.73))
                .summary("Approx " + loads + " laundry load(s). Estimated " + minutes + " min.")
                .build();
    }

    private String pickAppliance(Features f, String iconKey) {
        if ("ac".equals(iconKey)) {
            return "Air conditioner";
        }
        if (f.avgBrightness > 175 && f.colorVariance < 900) {
            return "Refrigerator";
        }
        if (f.aspectRatio > 1.2 && f.avgBrightness < 95) {
            return "Television";
        }
        if (f.edgeDensity > 0.11 && f.avgBrightness < 140) {
            return "Washing machine";
        }
        if (f.megapixels > 2.5) {
            return "Refrigerator";
        }
        return "Home appliance";
    }

    private Scene classifyScene(String iconKey, Features f) {
        if (iconKey.contains("utensil")) {
            return Scene.UTENSILS;
        }
        if (iconKey.contains("dish")) {
            return Scene.DISHES;
        }
        if (iconKey.contains("washroom")) {
            return Scene.WASHROOM;
        }
        if (iconKey.contains("laundry")) {
            return Scene.LAUNDRY;
        }
        if (iconKey.contains("vehicle")) {
            return Scene.VEHICLE;
        }
        if (iconKey.contains("appliance") || iconKey.contains("ac")) {
            return Scene.APPLIANCE;
        }
        if (iconKey.contains("room") || iconKey.contains("full-clean") || iconKey.contains("homekeeping")) {
            return Scene.ROOM;
        }
        if (f.edgeDensity > 0.14 && f.aspectRatio < 1.25) {
            return Scene.UTENSILS;
        }
        if (f.aspectRatio > 1.25 && f.megapixels > 0.8) {
            return Scene.ROOM;
        }
        if (f.avgBrightness > 160 && f.colorVariance < 1200) {
            return Scene.APPLIANCE;
        }
        return Scene.ROOM;
    }

    private Features extractFeatures(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        double aspect = w >= h ? (double) w / h : (double) h / w;
        double megapixels = (w * (double) h) / 1_000_000.0;

        long brightnessSum = 0;
        long varianceAcc = 0;
        int edgeCount = 0;
        int darkPatchCount = 0;
        int grid = 8;
        int cellW = Math.max(1, w / grid);
        int cellH = Math.max(1, h / grid);
        int[][] cellEdges = new int[grid][grid];

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int rgb = image.getRGB(x, y);
                int r = (rgb >> 16) & 0xff;
                int g = (rgb >> 8) & 0xff;
                int b = rgb & 0xff;
                int lum = (r + g + b) / 3;
                brightnessSum += lum;

                int nrgb = image.getRGB(x + 1, y);
                int nLum = (((nrgb >> 16) & 0xff) + ((nrgb >> 8) & 0xff) + (nrgb & 0xff)) / 3;
                int srgb = image.getRGB(x, y + 1);
                int sLum = (((srgb >> 16) & 0xff) + ((srgb >> 8) & 0xff) + (srgb & 0xff)) / 3;
                int grad = Math.abs(lum - nLum) + Math.abs(lum - sLum);
                if (grad > 42) {
                    edgeCount++;
                    int cx = Math.min(grid - 1, x / cellW);
                    int cy = Math.min(grid - 1, y / cellH);
                    cellEdges[cx][cy]++;
                }
                if (lum < 55) {
                    darkPatchCount++;
                }
            }
        }

        int samples = Math.max(1, (w - 2) * (h - 2));
        double avgBrightness = brightnessSum / (double) samples;

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
                int rgb = image.getRGB(x, y);
                int lum = (((rgb >> 16) & 0xff) + ((rgb >> 8) & 0xff) + (rgb & 0xff)) / 3;
                varianceAcc += (long) Math.pow(lum - avgBrightness, 2);
            }
        }
        double colorVariance = varianceAcc / samples;
        double edgeDensity = edgeCount / (double) samples;

        int activeCells = 0;
        int threshold = Math.max(20, edgeCount / (grid * grid * 2));
        for (int gy = 0; gy < grid; gy++) {
            for (int gx = 0; gx < grid; gx++) {
                if (cellEdges[gx][gy] > threshold) {
                    activeCells++;
                }
            }
        }

        String stainLevel = "LOW";
        double stainRatio = darkPatchCount / (double) samples;
        if (stainRatio > 0.08 || (edgeDensity > 0.16 && avgBrightness < 120)) {
            stainLevel = "HIGH";
        } else if (stainRatio > 0.035 || edgeDensity > 0.12) {
            stainLevel = "MEDIUM";
        }

        return new Features(aspect, megapixels, avgBrightness, colorVariance, edgeDensity, activeCells, stainLevel);
    }

    private int stainMinutes(String stainLevel) {
        return switch (stainLevel) {
            case "HIGH" -> 12;
            case "MEDIUM" -> 6;
            default -> 0;
        };
    }

    private double confidence(Features f, double base) {
        return clamp(base + (f.edgeDensity * 0.15) - (f.stainLevel.equals("HIGH") ? 0.05 : 0), 0.55, 0.95);
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Image file is required for analysis.");
        }
        String type = file.getContentType();
        if (type != null && ALLOWED.contains(type.toLowerCase(Locale.ROOT))) {
            return;
        }
        String name = file.getOriginalFilename();
        if (name != null && name.matches("(?i).+\\.(jpe?g|png|webp|gif|heic|heif)$")) {
            return;
        }
        if (type == null || type.startsWith("image/") || "application/octet-stream".equals(type)) {
            return;
        }
        throw new BadRequestException("Unsupported image type for analysis.");
    }

    private BufferedImage scaleForAnalysis(BufferedImage source) {
        int max = 800;
        int w = source.getWidth();
        int h = source.getHeight();
        if (w <= max && h <= max) {
            return toRgb(source);
        }
        double scale = Math.min((double) max / w, (double) max / h);
        int nw = Math.max(1, (int) (w * scale));
        int nh = Math.max(1, (int) (h * scale));
        BufferedImage scaled = new BufferedImage(nw, nh, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = scaled.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, nw, nh, null);
        g.dispose();
        return scaled;
    }

    private BufferedImage toRgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_RGB) {
            return source;
        }
        BufferedImage rgb = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D g = rgb.createGraphics();
        g.drawImage(source, 0, 0, null);
        g.dispose();
        return rgb;
    }

    private enum Scene {
        UTENSILS,
        DISHES,
        ROOM,
        WASHROOM,
        APPLIANCE,
        VEHICLE,
        LAUNDRY
    }

    private record Features(
            double aspectRatio,
            double megapixels,
            double avgBrightness,
            double colorVariance,
            double edgeDensity,
            int blobCells,
            String stainLevel) {}
}
