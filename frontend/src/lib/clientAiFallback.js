/** Offline fallback when the API analyze endpoint is unreachable. */
export function clientAiFallback(iconKey = 'full-cleaning') {
  const key = (iconKey || 'full-cleaning').toLowerCase()

  if (key.includes('utensil') || key.includes('dish')) {
    return {
      detectedType: 'UTENSILS',
      label: key.includes('dish') ? 'Dishes' : 'Utensils',
      quantity: 12,
      quantityUnit: key.includes('dish') ? 'items' : 'pieces',
      estimatedMinutes: 15,
      stainLevel: 'MEDIUM',
      confidence: 0.65,
      summary: 'Preview estimate from service type. Full analysis runs when you confirm booking.',
    }
  }
  if (key.includes('washroom')) {
    return {
      detectedType: 'ROOM',
      label: 'Washroom',
      quantity: 45,
      quantityUnit: 'sq ft',
      estimatedMinutes: 35,
      stainLevel: 'MEDIUM',
      confidence: 0.68,
      summary: 'Preview estimate for washroom cleaning.',
    }
  }
  if (key.includes('appliance') || key.includes('ac')) {
    return {
      detectedType: 'APPLIANCE',
      label: key.includes('ac') ? 'Air conditioner' : 'Home appliance',
      quantity: 1,
      quantityUnit: 'unit',
      estimatedMinutes: key.includes('ac') ? 30 : 15,
      stainLevel: 'LOW',
      confidence: 0.66,
      summary: 'Preview estimate for appliance service.',
    }
  }
  if (key.includes('vehicle')) {
    return {
      detectedType: 'VEHICLE',
      label: 'Car / compact vehicle',
      quantity: 1,
      quantityUnit: 'vehicle',
      estimatedMinutes: 40,
      stainLevel: 'LOW',
      confidence: 0.64,
      summary: 'Preview estimate for vehicle cleaning.',
    }
  }
  if (key.includes('laundry')) {
    return {
      detectedType: 'LAUNDRY',
      label: 'Laundry load',
      quantity: 2,
      quantityUnit: 'loads',
      estimatedMinutes: 35,
      stainLevel: 'LOW',
      confidence: 0.63,
      summary: 'Preview estimate for laundry service.',
    }
  }
  return {
    detectedType: 'ROOM',
    label: 'Room',
    quantity: 180,
    quantityUnit: 'sq ft',
    estimatedMinutes: 45,
    stainLevel: 'LOW',
    confidence: 0.67,
    summary: 'Preview estimate for room/area cleaning.',
  }
}
