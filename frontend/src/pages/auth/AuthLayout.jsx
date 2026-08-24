import { Outlet } from 'react-router-dom'
import Brand from '../../components/layout/Brand'

/**
 * Two panels: the form on paper, and a quiet statement of what the service
 * actually does on the brand block. No gradient hero — this is a utility
 * account, and the page should read like one.
 */
export default function AuthLayout() {
  return (
    <div className="grid min-h-dvh lg:grid-cols-[1fr_minmax(0,26rem)]">
      <aside className="relative hidden flex-col justify-between bg-brand p-10 text-white lg:flex dark:text-[#06201b]">
        <div className="flex items-center gap-2.5">
          <Brand size={30} className="text-white dark:text-[#06201b]" />
          <span className="text-[15px] font-semibold tracking-[-0.01em]">DESCO Portal</span>
        </div>

        <div className="max-w-md">
          <h1 className="text-[2.25rem] leading-[1.1] font-semibold tracking-[-0.025em]">
            Your supply, your bills, and every outage — in one place.
          </h1>
          <p className="mt-4 text-[14.5px] leading-relaxed opacity-80">
            Scheduled maintenance and emergency interruptions for your area, bill
            payment with receipts you can keep, and complaints you can actually
            follow to a resolution.
          </p>
        </div>

        <dl className="grid grid-cols-3 gap-6 border-t border-white/20 pt-6 dark:border-black/15">
          {[
            ['8', 'service areas'],
            ['24/7', 'outage alerts'],
            ['15 min', 'session security'],
          ].map(([value, caption]) => (
            <div key={caption}>
              <dt className="text-[1.35rem] font-semibold tracking-[-0.02em] tabular-nums">
                {value}
              </dt>
              <dd className="mt-0.5 text-[12px] opacity-70">{caption}</dd>
            </div>
          ))}
        </dl>
      </aside>

      <main className="flex items-center justify-center bg-paper px-5 py-10 sm:px-8">
        <div className="w-full max-w-sm">
          <div className="mb-8 flex items-center gap-2.5 text-brand lg:hidden">
            <Brand size={28} />
            <span className="text-[15px] font-semibold text-ink">DESCO Portal</span>
          </div>
          <Outlet />
        </div>
      </main>
    </div>
  )
}
