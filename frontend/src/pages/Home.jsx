const AREAS = [
  'Uttara',
  'Gulshan',
  'Banani',
  'Dhanmondi',
  'Bashundhara',
  'Mirpur',
  'Banasree',
  'Baridhara',
]

function Home() {
  return (
    <main className="min-h-screen bg-gray-950 text-gray-100">
      <div className="mx-auto max-w-4xl px-6 py-20 text-center">
        <p className="text-sm uppercase tracking-widest text-sky-400">
          DESCO Simulation
        </p>
        <h1 className="mt-3 text-4xl font-semibold sm:text-5xl">
          Smart Electricity Management System
        </h1>
        <p className="mx-auto mt-4 max-w-2xl text-gray-400">
          Outage notifications, complaint tracking, and bill payment
          simulation across a microservice backend.
        </p>

        <div className="mt-10 flex flex-wrap justify-center gap-2">
          {AREAS.map((area) => (
            <span
              key={area}
              className="rounded-full border border-gray-800 px-3 py-1 text-sm text-gray-300"
            >
              {area}
            </span>
          ))}
        </div>
      </div>
    </main>
  )
}

export default Home
