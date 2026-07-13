import { motion, AnimatePresence } from 'framer-motion'
import { useInstallPwa } from '../hooks/useInstallPwa'

export default function InstallPwaBanner() {
  const { canInstall, installed, install, dismiss } = useInstallPwa()

  return (
    <AnimatePresence>
      {canInstall && (
        <motion.div
          initial={{ opacity: 0, y: 24 }}
          animate={{ opacity: 1, y: 0 }}
          exit={{ opacity: 0, y: 24 }}
          transition={{ duration: 0.35, ease: [0.22, 1, 0.36, 1] }}
          className="fixed bottom-4 left-4 right-4 z-50 mx-auto max-w-lg rounded-2xl border border-white/10 bg-slate-900/95 p-4 shadow-2xl backdrop-blur-md sm:left-auto sm:right-6"
          role="region"
          aria-label="Install Zivro app"
        >
          <div className="flex items-start gap-3">
            <img
              src="/icons/icon-192x192.png"
              alt=""
              className="h-12 w-12 shrink-0 rounded-xl"
            />
            <div className="min-w-0 flex-1">
              <p className="text-sm font-semibold text-white">Install Zivro</p>
              <p className="mt-1 text-xs text-slate-400">
                Add to your home screen for quick access — works like a native app on Android and iOS.
              </p>
              <div className="mt-3 flex flex-wrap gap-2">
                <button
                  type="button"
                  onClick={install}
                  className="rounded-xl bg-gradient-to-r from-zivro-blue to-zivro-green px-4 py-2 text-xs font-semibold text-zivro-ink"
                >
                  Install app
                </button>
                <button
                  type="button"
                  onClick={dismiss}
                  className="rounded-xl border border-white/10 px-4 py-2 text-xs font-medium text-slate-300 hover:bg-white/5"
                >
                  Not now
                </button>
              </div>
            </div>
          </div>
        </motion.div>
      )}

      {installed && (
        <span className="sr-only">Zivro is installed as an app.</span>
      )}
    </AnimatePresence>
  )
}
