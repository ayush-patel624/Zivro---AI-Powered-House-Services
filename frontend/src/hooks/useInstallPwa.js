import { useCallback, useEffect, useState } from 'react'

export function useInstallPwa() {
  const [deferredPrompt, setDeferredPrompt] = useState(null)
  const [installed, setInstalled] = useState(false)
  const [dismissed, setDismissed] = useState(false)

  useEffect(() => {
    const standalone =
      window.matchMedia('(display-mode: standalone)').matches ||
      window.navigator.standalone === true

    if (standalone) {
      setInstalled(true)
      return undefined
    }

    try {
      if (localStorage.getItem('zivro_pwa_install_dismissed') === '1') {
        setDismissed(true)
      }
    } catch {
      /* private browsing */
    }

    const onBeforeInstall = (event) => {
      event.preventDefault()
      setDeferredPrompt(event)
    }

    const onInstalled = () => {
      setInstalled(true)
      setDeferredPrompt(null)
    }

    window.addEventListener('beforeinstallprompt', onBeforeInstall)
    window.addEventListener('appinstalled', onInstalled)

    return () => {
      window.removeEventListener('beforeinstallprompt', onBeforeInstall)
      window.removeEventListener('appinstalled', onInstalled)
    }
  }, [])

  const install = useCallback(async () => {
    if (!deferredPrompt) return false
    deferredPrompt.prompt()
    const { outcome } = await deferredPrompt.userChoice
    setDeferredPrompt(null)
    if (outcome === 'accepted') {
      setInstalled(true)
      return true
    }
    return false
  }, [deferredPrompt])

  const dismiss = useCallback(() => {
    setDismissed(true)
    try {
      localStorage.setItem('zivro_pwa_install_dismissed', '1')
    } catch {
      /* private browsing */
    }
  }, [])

  return {
    canInstall: Boolean(deferredPrompt) && !installed && !dismissed,
    installed,
    install,
    dismiss,
  }
}
