import { mkdir, writeFile } from 'node:fs/promises'
import { dirname, join } from 'node:path'
import { fileURLToPath } from 'node:url'
import sharp from 'sharp'

const root = join(dirname(fileURLToPath(import.meta.url)), '..')
const publicDir = join(root, 'public')
const iconsDir = join(publicDir, 'icons')
const svgPath = join(publicDir, 'favicon.svg')

const sizes = [72, 96, 128, 144, 152, 192, 384, 512]

await mkdir(iconsDir, { recursive: true })

for (const size of sizes) {
  await sharp(svgPath)
    .resize(size, size)
    .png()
    .toFile(join(iconsDir, `icon-${size}x${size}.png`))
}

// Maskable icon: extra padding so Android adaptive icons don't clip the logo.
await sharp(svgPath)
  .resize(432, 432)
  .extend({
    top: 40,
    bottom: 40,
    left: 40,
    right: 40,
    background: '#020617',
  })
  .png()
  .toFile(join(iconsDir, 'icon-maskable-512x512.png'))

await sharp(svgPath)
  .resize(180, 180)
  .png()
  .toFile(join(iconsDir, 'apple-touch-icon.png'))

console.log('Generated PWA icons in public/icons/')
