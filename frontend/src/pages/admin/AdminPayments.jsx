import { useState } from 'react'
import Badge from '../../components/ui/Badge'
import { EmptyState, ErrorState, SkeletonRows } from '../../components/ui/States'
import { Surface } from '../../components/ui/Surface'
import { Pagination, TableWrap, Td, Th, Tr } from '../../components/ui/Table'
import { formatBillMonth, formatDateTime, shortId, taka } from '../../lib/format'
import { useAsync } from '../../lib/useAsync'
import { listPayments } from '../../services/admin.service'

export default function AdminPayments() {
  const [page, setPage] = useState(0)
  const payments = useAsync(() => listPayments({ page, size: 20 }), [page])
  const rows = payments.data?.content || []

  return (
    <div className="grid gap-5">
      <header className="animate-rise">
        <h1 className="text-[1.55rem] font-semibold tracking-[-0.022em] text-ink">
          Payment ledger
        </h1>
        <p className="mt-1 text-[13.5px] text-muted">
          Every payment across every account, newest first.
        </p>
      </header>

      <Surface className="overflow-hidden">
        {payments.loading ? (
          <div className="p-5">
            <SkeletonRows rows={6} />
          </div>
        ) : payments.error ? (
          <ErrorState error={payments.error} onRetry={payments.reload} />
        ) : rows.length === 0 ? (
          <EmptyState
            title="No payments recorded"
            description="The ledger fills as customers settle their bills."
          />
        ) : (
          <>
            <TableWrap>
              <thead>
                <tr>
                  <Th>Transaction</Th>
                  <Th>Account</Th>
                  <Th>Bill month</Th>
                  <Th>Method</Th>
                  <Th>Paid at</Th>
                  <Th>Status</Th>
                  <Th align="right">Amount</Th>
                </tr>
              </thead>
              <tbody>
                {rows.map((payment) => (
                  <Tr key={payment.id}>
                    <Td mono className="text-[12.5px]">
                      {payment.transactionId}
                    </Td>
                    <Td mono className="text-[12.5px] text-muted">
                      {shortId(payment.userId)}
                    </Td>
                    <Td>{formatBillMonth(payment.billMonth)}</Td>
                    <Td className="text-muted">{payment.paymentMethod}</Td>
                    <Td mono className="text-[12.5px] whitespace-nowrap">
                      {formatDateTime(payment.paidAt || payment.createdAt)}
                    </Td>
                    <Td>
                      <Badge value={payment.status} />
                    </Td>
                    {/* Money is right-aligned and tabular so a column of
                        amounts can be scanned and compared by eye. */}
                    <Td align="right" mono className="font-semibold">
                      {taka(payment.amount)}
                    </Td>
                  </Tr>
                ))}
              </tbody>
            </TableWrap>
            <Pagination {...payments.data} onPage={setPage} />
          </>
        )}
      </Surface>
    </div>
  )
}
