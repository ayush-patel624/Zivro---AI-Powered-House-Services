export const SERVICE_ICONS = {
  'full-cleaning': { emoji: '🏠', label: 'Full cleaning' },
  'room-cleaning': { emoji: '🛏️', label: 'Room' },
  washroom: { emoji: '🚿', label: 'Washroom' },
  utensils: { emoji: '🍴', label: 'Utensils' },
  dishes: { emoji: '🍽️', label: 'Dishes' },
  laundry: { emoji: '👕', label: 'Laundry' },
  homekeeping: { emoji: '🧹', label: 'Home keeping' },
  cooking: { emoji: '👨‍🍳', label: 'Cooking' },
  painting: { emoji: '🎨', label: 'Painting' },
  packing: { emoji: '📦', label: 'Packing & movers' },
  vehicle: { emoji: '🚗', label: 'Vehicle cleaning' },
  hardware: { emoji: '🛠️', label: 'Hardware setup' },
  appliance: { emoji: '🧊', label: 'Appliance cleaning' },
  ac: { emoji: '❄️', label: 'AC service' },
  plumbing: { emoji: '🔧', label: 'Plumbing' },
}

export function getServiceIcon(iconKey) {
  return SERVICE_ICONS[iconKey] || { emoji: '✨', label: 'Service' }
}

export function mediaUrl(url) {
  if (!url) return ''
  if (url.startsWith('http://') || url.startsWith('https://')) return url
  const base = import.meta.env.VITE_API_URL || 'http://localhost:8081'
  return `${base.replace(/\/$/, '')}${url.startsWith('/') ? url : `/${url}`}`
}
