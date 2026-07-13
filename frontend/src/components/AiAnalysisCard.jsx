import { motion } from 'framer-motion'

export default function AiAnalysisCard({ analysis, loading, error }) {
  if (loading) {
    return (
      <div className="rounded-xl border border-zivro-blue/20 bg-zivro-blue/5 px-4 py-3 text-sm text-slate-300">
        Analyzing photo with AI…
      </div>
    )
  }
  if (error && !analysis) {
    return (
      <div className="rounded-xl border border-amber-500/30 bg-amber-500/10 px-4 py-3 text-sm text-amber-100">
        {error}
      </div>
    )
  }
  if (!analysis) return null

  const stainColors = {
    LOW: 'text-zivro-green',
    MEDIUM: 'text-amber-300',
    HIGH: 'text-red-300',
  }

  return (
    <motion.div
      initial={{ opacity: 0, y: 6 }}
      animate={{ opacity: 1, y: 0 }}
      className="rounded-xl border border-zivro-blue/30 bg-gradient-to-br from-zivro-blue/10 to-zivro-green/5 px-4 py-4"
    >
      <p className="text-xs font-semibold uppercase tracking-wide text-zivro-blue">AI detection</p>
      <p className="mt-2 text-lg font-semibold text-white">{analysis.label}</p>
      <div className="mt-3 grid grid-cols-2 gap-3 text-sm">
        <div>
          <p className="text-slate-500">Quantity</p>
          <p className="font-medium text-white">
            ~{analysis.quantity} {analysis.quantityUnit}
          </p>
        </div>
        <div>
          <p className="text-slate-500">Est. time</p>
          <p className="font-medium text-white">{analysis.estimatedMinutes} min</p>
        </div>
        <div>
          <p className="text-slate-500">Stain level</p>
          <p className={`font-medium ${stainColors[analysis.stainLevel] || 'text-slate-300'}`}>
            {analysis.stainLevel}
          </p>
        </div>
        <div>
          <p className="text-slate-500">Confidence</p>
          <p className="font-medium text-white">{Math.round((analysis.confidence || 0) * 100)}%</p>
        </div>
      </div>
      {analysis.summary && <p className="mt-3 text-xs text-slate-400">{analysis.summary}</p>}
    </motion.div>
  )
}
