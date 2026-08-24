import { useState } from 'react'
import Badge from '../../components/ui/Badge'
import Button from '../../components/ui/Button'
import { Select } from '../../components/ui/Field'
import Modal from '../../components/ui/Modal'
import { EmptyState, ErrorState, SkeletonRows } from '../../components/ui/States'
import { Surface } from '../../components/ui/Surface'
import { Pagination, TableWrap, Td, Th, Tr } from '../../components/ui/Table'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { areaLabel } from '../../lib/enums'
import { formatBillMonth, formatDate, shortId, taka } from '../../lib/format'
import { useAsync } from '../../lib/useAsync'
import { getUserPayments, listUsers, setUserActive } from '../../services/admin.service'

export default function AdminUsers() {
  const toast = useToast()
  const { userId: myId } = useAuth()

  const [filter, setFilter] = useState('')
  const [page, setPage] = useState(0)
  const size = 10

  const users = useAsync(
    () => listUsers({ isActive: filter, page, size }),
    [filter, page],
  )
  const [pending, setPending] = useState(null)
  const [ledgerFor, setLedgerFor] = useState(null)

  async function onToggle(user) {
    setPending(user.id)
    try {
      const updated = await setUserActive(user.id, !user.isActive)
      users.setData((current) => ({
        ...current,
        content: (current?.content || []).map((u) => (u.id === updated.id ? updated : u)),
      }))
      toast.success(
        `${updated.email} ${updated.isActive ? 'reactivated' : 'deactivated'}.`,
      )
    } catch (error) {
      // The last-admin guard is the system refusing to let itself be locked
      // out — worth stating for longer than a normal error.
      if (error.code === 'LAST_ADMIN') toast.error(error.message, { duration: 9000 })
      else toast.error(error.message)
    } finally {
      setPending(null)
    }
  }

  const rows = users.data?.content || []

  return (
    <div className="grid gap-5">
      <header className="animate-rise flex flex-wrap items-end justify-between gap-3">
        <div>
          <h1 className="text-[1.55rem] font-semibold tracking-[-0.022em] text-ink">
            User management
          </h1>
          <p className="mt-1 text-[13.5px] text-muted">
            Activate or deactivate accounts and inspect their payment history.
          </p>
        </div>
        <Select
          value={filter}
          onChange={(event) => {
            setFilter(event.target.value)
            setPage(0)
          }}
          className="w-44"
          aria-label="Filter by status"
        >
          <option value="">All accounts</option>
          <option value="true">Active only</option>
          <option value="false">Deactivated only</option>
        </Select>
      </header>

      <Surface className="overflow-hidden">
        {users.loading ? (
          <div className="p-5">
            <SkeletonRows rows={5} />
          </div>
        ) : users.error ? (
          <ErrorState error={users.error} onRetry={users.reload} />
        ) : rows.length === 0 ? (
          <EmptyState title="No accounts match" description="Try a different filter." />
        ) : (
          <>
            <TableWrap>
              <thead>
                <tr>
                  <Th>Email</Th>
                  <Th>Role</Th>
                  <Th>Area</Th>
                  <Th>Registered</Th>
                  <Th>Status</Th>
                  <Th align="right">Actions</Th>
                </tr>
              </thead>
              <tbody>
                {rows.map((user) => (
                  <Tr key={user.id}>
                    <Td>
                      <span className="font-medium text-ink">{user.email}</span>
                      <span className="tnum block text-[11.5px] text-faint">
                        {shortId(user.id)}
                        {user.id === myId && ' · you'}
                      </span>
                    </Td>
                    <Td>
                      {user.role === 'ADMIN' ? (
                        <Badge tone="info">Admin</Badge>
                      ) : (
                        <span className="text-muted">User</span>
                      )}
                    </Td>
                    <Td>{areaLabel(user.area)}</Td>
                    <Td mono>{formatDate(user.createdAt)}</Td>
                    <Td>
                      <Badge tone={user.isActive ? 'positive' : 'neutral'} dot>
                        {user.isActive ? 'Active' : 'Deactivated'}
                      </Badge>
                    </Td>
                    <Td align="right">
                      <div className="flex justify-end gap-1.5">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => setLedgerFor(user)}
                        >
                          Payments
                        </Button>
                        <Button
                          variant={user.isActive ? 'secondary' : 'primary'}
                          size="sm"
                          loading={pending === user.id}
                          onClick={() => onToggle(user)}
                        >
                          {user.isActive ? 'Deactivate' : 'Reactivate'}
                        </Button>
                      </div>
                    </Td>
                  </Tr>
                ))}
              </tbody>
            </TableWrap>
            <Pagination {...users.data} onPage={setPage} />
          </>
        )}
      </Surface>

      <UserLedger user={ledgerFor} onClose={() => setLedgerFor(null)} />
    </div>
  )
}

function UserLedger({ user, onClose }) {
  const payments = useAsync(() => getUserPayments(user.id), [user?.id], {
    enabled: Boolean(user),
  })
  const rows = payments.data || []
  const total = rows
    .filter((p) => p.status === 'SUCCESS')
    .reduce((sum, p) => sum + Number(p.amount || 0), 0)

  return (
    <Modal
      open={Boolean(user)}
      onClose={onClose}
      size="lg"
      title="Payment history"
      description={user?.email}
      footer={
        <Button variant="secondary" onClick={onClose}>
          Close
        </Button>
      }
    >
      {payments.loading ? (
        <SkeletonRows rows={3} />
      ) : payments.error ? (
        <ErrorState error={payments.error} onRetry={payments.reload} />
      ) : rows.length === 0 ? (
        <EmptyState title="No payments" description="This account has never paid a bill." />
      ) : (
        <>
          <p className="mb-3 text-[13px] text-muted">
            <span className="tnum font-semibold text-ink">{taka(total)}</span> collected
            across {rows.length} {rows.length === 1 ? 'payment' : 'payments'}.
          </p>
          <ul className="divide-y divide-line border-t border-line">
            {rows.map((payment) => (
              <li key={payment.id} className="flex items-center gap-3 py-2.5">
                <div className="min-w-0 flex-1">
                  <p className="text-[13.5px] font-medium text-ink">
                    {formatBillMonth(payment.billMonth)}
                  </p>
                  <p className="tnum truncate text-[11.5px] text-faint">
                    {payment.transactionId}
                  </p>
                </div>
                <Badge value={payment.status} />
                <p className="tnum w-24 text-right text-[13.5px] font-semibold text-ink">
                  {taka(payment.amount)}
                </p>
              </li>
            ))}
          </ul>
        </>
      )}
    </Modal>
  )
}
