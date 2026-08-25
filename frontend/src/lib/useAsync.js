import { useCallback, useEffect, useRef, useState } from 'react'

/**
 * Load-on-mount with retry. Every read in this app has the same three states
 * and the same reload requirement, so they share one hook rather than each
 * page reinventing a `loading` flag and forgetting the unmount guard.
 *
 * `deps` behaves like a `useEffect` dependency array. Pass `enabled: false`
 * to hold the call until a prerequisite (usually `userId`) exists.
 */
export function useAsync(fn, deps = [], { enabled = true } = {}) {
  const [state, setState] = useState({ data: null, error: null, loading: enabled })
  const alive = useRef(true)
  const callRef = useRef(fn)
  callRef.current = fn

  // Only the latest run may write state — a slow first request must not
  // overwrite a fast second one with stale data.
  const runId = useRef(0)

  const run = useCallback(async () => {
    const id = ++runId.current
    setState((s) => ({ ...s, loading: true, error: null }))
    try {
      const data = await callRef.current()
      if (!alive.current || id !== runId.current) return
      setState({ data, error: null, loading: false })
    } catch (error) {
      if (!alive.current || id !== runId.current) return
      setState({ data: null, error, loading: false })
    }
  }, [])

  useEffect(() => {
    alive.current = true
    return () => {
      alive.current = false
    }
  }, [])

  useEffect(() => {
    if (!enabled) {
      setState({ data: null, error: null, loading: false })
      return
    }
    run()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [enabled, ...deps])

  const setData = useCallback((updater) => {
    setState((s) => ({
      ...s,
      data: typeof updater === 'function' ? updater(s.data) : updater,
    }))
  }, [])

  return { ...state, reload: run, setData }
}
