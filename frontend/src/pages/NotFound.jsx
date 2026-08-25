import { Link } from 'react-router-dom'
import Button from '../components/ui/Button'
import { useAuth } from '../context/AuthContext'

export default function NotFound() {
  const { isAuthenticated, isAdmin } = useAuth()
  const home = !isAuthenticated ? '/login' : isAdmin ? '/admin' : '/'

  return (
    <div className="grid min-h-dvh place-items-center bg-paper px-6">
      <div className="animate-rise max-w-sm text-center">
        <p className="text-[12px] font-semibold tracking-[0.08em] text-faint uppercase">
          404
        </p>
        <h1 className="mt-2 text-[1.5rem] font-semibold tracking-[-0.02em] text-ink">
          This page doesn’t exist
        </h1>
        <p className="mt-2 text-[14px] text-muted">
          The link may be out of date, or the page may have moved.
        </p>
        <Button as={Link} to={home} className="mt-6">
          Back to safety
        </Button>
      </div>
    </div>
  )
}
