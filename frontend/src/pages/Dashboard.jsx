import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api";

export function Status({ value }) {
  return (
    <span className={"status " + String(value).toLowerCase()}>
      {value}
    </span>
  );
}

export default function Dashboard() {

  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  async function loadDashboard() {

    try {

      setLoading(true);
      setError("");

      const response = await api.get("/work-orders");

      const data = Array.isArray(response.data)
        ? response.data
        : response.data.content || [];

      setOrders(data);

    } catch (error) {

      setError(
        error.response?.data?.message ||
        "Could not load dashboard data."
      );

    } finally {

      setLoading(false);

    }
  }

  // Load when dashboard opens
  useEffect(() => {

    loadDashboard();

    // Automatically refresh every 5 seconds
    const interval = setInterval(() => {
      loadDashboard();
    }, 5000);

    return () => clearInterval(interval);

  }, []);

  const newCount = orders.filter(
    order => order.status === "NEW"
  ).length;

  const assignedCount = orders.filter(
    order => order.status === "ASSIGNED"
  ).length;

  const progressCount = orders.filter(
    order => order.status === "IN_PROGRESS"
  ).length;

  const holdCount = orders.filter(
    order => order.status === "ON_HOLD"
  ).length;

  const completedCount = orders.filter(
    order => order.status === "COMPLETED"
  ).length;

  const closedCount = orders.filter(
    order => order.status === "CLOSED"
  ).length;

  return (
    <>
      <div className="heading">

        <div>
          <small>OVERVIEW</small>

          <h2>
            Operations Dashboard
          </h2>

          <p>
            Monitor service activity from one place.
          </p>
        </div>

        <div style={{ display: "flex", gap: "10px" }}>

          <button
            className="secondary"
            onClick={loadDashboard}
          >
            Refresh
          </button>

          <Link
            className="btn"
            to="/work-orders"
          >
            View Work Orders
          </Link>

        </div>

      </div>

      {error && (
        <div className="error">
          {error}
        </div>
      )}

      {/* DASHBOARD COUNTS */}

      <div className="stats">

        <div>
          TOTAL WORK ORDERS
          <strong>{orders.length}</strong>
        </div>

        <div>
          NEW
          <strong>{newCount}</strong>
        </div>

        <div>
          ASSIGNED
          <strong>{assignedCount}</strong>
        </div>

        <div>
          IN PROGRESS
          <strong>{progressCount}</strong>
        </div>

        <div>
          COMPLETED
          <strong>{completedCount}</strong>
        </div>

      </div>

      {/* STATUS PIPELINE */}

      <div className="panel">

        <h3>
          Work Order Progress
        </h3>

        <div className="progress-grid">

          <Progress
            name="NEW"
            count={newCount}
            total={orders.length}
          />

          <Progress
            name="ASSIGNED"
            count={assignedCount}
            total={orders.length}
          />

          <Progress
            name="IN PROGRESS"
            count={progressCount}
            total={orders.length}
          />

          <Progress
            name="ON HOLD"
            count={holdCount}
            total={orders.length}
          />

          <Progress
            name="COMPLETED"
            count={completedCount}
            total={orders.length}
          />

          <Progress
            name="CLOSED"
            count={closedCount}
            total={orders.length}
          />

        </div>

      </div>

      {/* RECENT WORK ORDERS */}

      <div className="panel">

        <div className="panel-header">

          <h3>
            Recent Work Orders
          </h3>

          <Link to="/work-orders">
            See All
          </Link>

        </div>

        {loading ? (

          <p className="muted">
            Loading work orders...
          </p>

        ) : orders.length === 0 ? (

          <p className="muted">
            No work orders available.
          </p>

        ) : (

          <div className="table">

            <table>

              <thead>

                <tr>
                  <th>Code</th>
                  <th>Title</th>
                  <th>Customer</th>
                  <th>Priority</th>
                  <th>Status</th>
                  <th>Technician</th>
                </tr>

              </thead>

              <tbody>

                {orders
                  .slice()
                  .reverse()
                  .slice(0, 10)
                  .map(order => (

                    <tr key={order.id}>

                      <td>

                        <Link
                          to={`/work-orders/${order.id}`}
                        >
                          {order.code ||
                            `WO-${order.id}`}
                        </Link>

                      </td>

                      <td>
                        {order.title}
                      </td>

                      <td>
                        {order.customerName || "-"}
                      </td>

                      <td>
                        {order.priority}
                      </td>

                      <td>
                        <Status
                          value={order.status}
                        />
                      </td>

                      <td>
                        {order.assigneeName ||
                          "Unassigned"}
                      </td>

                    </tr>

                  ))}

              </tbody>

            </table>

          </div>

        )}

      </div>

    </>
  );
}


function Progress({
  name,
  count,
  total
}) {

  const percentage =
    total > 0
      ? Math.round((count / total) * 100)
      : 0;

  return (

    <div className="progress-item">

      <div className="progress-label">

        <span>
          {name}
        </span>

        <strong>
          {count}
        </strong>

      </div>

      <div className="progress-bar">

        <div
          className="progress-fill"
          style={{
            width: `${percentage}%`
          }}
        />

      </div>

    </div>

  );
}