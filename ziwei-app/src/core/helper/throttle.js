/**
 * Throttle utility — limits function calls to once per interval
 * @param {Function} fn - The function to throttle
 * @param {number} [wait=500] - Minimum time between calls in ms
 * @param {Object} [options]
 * @param {boolean} [options.leading=true] - Call on the leading edge
 * @param {boolean} [options.trailing=true] - Call on the trailing edge
 * @returns {Function}
 */
export default function throttle(fn, wait = 500, options = {}) {
  const { leading = true, trailing = true } = options
  let timer = null
  let lastArgs = null
  let lastTime = 0

  function invoke() {
    if (lastArgs && trailing) {
      fn.apply(this, lastArgs)
      lastTime = Date.now()
    }
  }

  return function (...args) {
    const now = Date.now()

    if (!lastTime && !leading) {
      lastTime = now
    }

    const remaining = wait - (now - lastTime)

    if (remaining <= 0) {
      if (timer) {
        clearTimeout(timer)
        timer = null
      }
      fn.apply(this, args)
      lastTime = now
    } else if (!timer && trailing) {
      lastArgs = args
      timer = setTimeout(() => {
        timer = null
        invoke()
      }, remaining)
    }
  }
}
