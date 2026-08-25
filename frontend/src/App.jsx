import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { RequireAdmin, RequireAnonymous, RequireCustomer } from './components/Guards'
import AdminShell from './components/layout/AdminShell'
import CustomerShell from './components/layout/CustomerShell'
import Toaster from './components/ui/Toaster'
import { AuthProvider } from './context/AuthContext'
import { ToastProvider } from './context/ToastContext'
import NotFound from './pages/NotFound'
import AdminComplaints from './pages/admin/AdminComplaints'
import AdminDashboard from './pages/admin/AdminDashboard'
import AdminOutages from './pages/admin/AdminOutages'
import AdminPayments from './pages/admin/AdminPayments'
import AdminUsers from './pages/admin/AdminUsers'
import AuthLayout from './pages/auth/AuthLayout'
import Login from './pages/auth/Login'
import Register from './pages/auth/Register'
import Alerts from './pages/customer/Alerts'
import Complaints from './pages/customer/Complaints'
import Dashboard from './pages/customer/Dashboard'
import Payments from './pages/customer/Payments'
import Profile from './pages/customer/Profile'

/**
 * Two experiences behind one router, separated by shell rather than by
 * conditional rendering inside a shared layout: a USER can never land in the
 * admin shell even for a frame, because the admin routes only mount beneath
 * `RequireAdmin`.
 */
export default function App() {
  return (
    <BrowserRouter>
      <ToastProvider>
        <AuthProvider>
          <Routes>
            <Route element={<RequireAnonymous />}>
              <Route element={<AuthLayout />}>
                <Route path="/login" element={<Login />} />
                <Route path="/register" element={<Register />} />
              </Route>
            </Route>

            <Route element={<RequireCustomer />}>
              <Route element={<CustomerShell />}>
                <Route index element={<Dashboard />} />
                <Route path="/pay" element={<Payments />} />
                <Route path="/complaints" element={<Complaints />} />
                <Route path="/alerts" element={<Alerts />} />
                <Route path="/profile" element={<Profile />} />
              </Route>
            </Route>

            <Route element={<RequireAdmin />}>
              <Route path="/admin" element={<AdminShell />}>
                <Route index element={<AdminDashboard />} />
                <Route path="users" element={<AdminUsers />} />
                <Route path="outages" element={<AdminOutages />} />
                <Route path="complaints" element={<AdminComplaints />} />
                <Route path="payments" element={<AdminPayments />} />
                {/* Same record, same component — rendered inside the admin
                    shell so an admin never gets dropped into the customer app
                    just to change their own name. */}
                <Route path="profile" element={<Profile variant="admin" />} />
              </Route>
            </Route>

            <Route path="/index.html" element={<Navigate to="/" replace />} />
            <Route path="*" element={<NotFound />} />
          </Routes>

          <Toaster />
        </AuthProvider>
      </ToastProvider>
    </BrowserRouter>
  )
}
