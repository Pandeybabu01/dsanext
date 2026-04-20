export default function Footer() {
  return (
    <footer className="border-t border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-900 py-4 px-6">
      <div className="max-w-7xl mx-auto flex flex-col sm:flex-row items-center justify-between gap-2">
        <p className="text-xs text-slate-400 dark:text-slate-500">
          © {new Date().getFullYear()} DSANext. Master DSA. Land the job.
        </p>
        <p className="text-xs text-slate-400 dark:text-slate-500">
          Built with ⚡ React + Spring Boot
        </p>
      </div>
    </footer>
  )
}
