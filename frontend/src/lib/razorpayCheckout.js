/** @returns {Promise<void>} */
export function loadRazorpayScript() {
  if (typeof window === 'undefined') return Promise.resolve()
  if (window.Razorpay) return Promise.resolve()
  return new Promise((resolve, reject) => {
    const existing = document.querySelector('script[data-zivro-razorpay]')
    if (existing) {
      if (window.Razorpay) {
        resolve()
        return
      }
      existing.addEventListener('load', () => resolve())
      existing.addEventListener('error', reject)
      return
    }
    const s = document.createElement('script')
    s.src = 'https://checkout.razorpay.com/v1/checkout.js'
    s.async = true
    s.dataset.zivroRazorpay = '1'
    s.onload = () => resolve()
    s.onerror = () => reject(new Error('Failed to load Razorpay Checkout'))
    document.body.appendChild(s)
  })
}

/**
 * @param {object} opts
 * @param {string} opts.key
 * @param {string} opts.orderId
 * @param {string} [opts.name]
 * @param {string} [opts.description]
 * @param {Record<string, string>} [opts.prefill]
 * @param {(response: { razorpay_order_id: string; razorpay_payment_id: string; razorpay_signature: string }) => void} opts.handler
 */
export async function openRazorpayCheckout(opts) {
  await loadRazorpayScript()
  const key = opts.key || import.meta.env.VITE_RAZORPAY_KEY_ID
  if (!key) {
    throw new Error('Razorpay key is not configured (API public-config or VITE_RAZORPAY_KEY_ID).')
  }
  const options = {
    key,
    order_id: opts.orderId,
    name: opts.name || 'Zivro',
    description: opts.description || 'Service booking',
    handler: opts.handler,
    prefill: opts.prefill || {},
    theme: { color: '#38bdf8' },
    modal: {
      ondismiss() {},
    },
  }
  const rzp = new window.Razorpay(options)
  rzp.open()
}
