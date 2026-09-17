import { useEffect, useState } from "react";
import api from "../api";

export default function Sites() {
  const [sites, setSites] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [form, setForm] = useState({
    name: "",
    address: "",
    city: "Hyderabad",
    postalCode: "",
    customerId: ""
  });
  const [message, setMessage] = useState("");

  async function loadCustomers() {
    try {
      const response = await api.get("/customers");
      setCustomers(response.data);
    } catch (error) {
      setMessage(
        error.response?.data?.message || "Could not load customers."
      );
    }
  }

  async function loadSites(customerId) {
    if (!customerId) {
      setSites([]);
      return;
    }

    try {
      const response = await api.get(
        `/customers/${customerId}/sites`
      );

      setSites(response.data);
    } catch (error) {
      setMessage(
        error.response?.data?.message || "Could not load sites."
      );
    }
  }

  useEffect(() => {
    loadCustomers();
  }, []);

  function handleCustomerChange(event) {
    const customerId = event.target.value;

    setForm({
      ...form,
      customerId
    });

    loadSites(customerId);
  }

  async function createSite(event) {
    event.preventDefault();
    setMessage("");

    if (!form.customerId) {
      setMessage("Please select a customer.");
      return;
    }

    try {
      await api.post(
        `/customers/${form.customerId}/sites`,
        {
          name: form.name,
          address: form.address,
          city: form.city,
          postalCode: form.postalCode,
          customerId: Number(form.customerId)
        }
      );

      setMessage("Site created successfully.");

      const customerId = form.customerId;

      setForm({
        name: "",
        address: "",
        city: "Hyderabad",
        postalCode: "",
        customerId
      });

      loadSites(customerId);
    } catch (error) {
      setMessage(
        error.response?.data?.message || "Could not create site."
      );
    }
  }

  return (
    <>
      <div className="heading">
        <div>
          <small>LOCATION MANAGEMENT</small>
          <h2>Sites</h2>
          <p>Manage customer service locations.</p>
        </div>
      </div>

      {message && (
        <div className="notice">
          {message}
        </div>
      )}

      <div className="two">

        {/* CREATE SITE */}
        <div className="panel">
          <h3>Add Site</h3>

          <form onSubmit={createSite}>

            <label>
              Site Name
              <input
                required
                value={form.name}
                onChange={(e) =>
                  setForm({
                    ...form,
                    name: e.target.value
                  })
                }
              />
            </label>

            <label>
              Address
              <input
                value={form.address}
                onChange={(e) =>
                  setForm({
                    ...form,
                    address: e.target.value
                  })
                }
              />
            </label>

            <label>
              City
              <input
                value={form.city}
                onChange={(e) =>
                  setForm({
                    ...form,
                    city: e.target.value
                  })
                }
              />
            </label>

            <label>
              Postal Code
              <input
                value={form.postalCode}
                onChange={(e) =>
                  setForm({
                    ...form,
                    postalCode: e.target.value
                  })
                }
              />
            </label>

            <label>
              Customer

              <select
                required
                value={form.customerId}
                onChange={handleCustomerChange}
              >
                <option value="">
                  Select customer
                </option>

                {customers.map((customer) => (
                  <option
                    key={customer.id}
                    value={customer.id}
                  >
                    {customer.name}
                  </option>
                ))}
              </select>
            </label>

            <button type="submit">
              Create Site
            </button>

          </form>
        </div>

        {/* SITE LIST */}
        <div className="panel table">
          <h3>
            {form.customerId
              ? "Customer Sites"
              : "Select a customer"}
          </h3>

          {form.customerId && sites.length === 0 ? (
            <p className="muted">
              No sites found for this customer.
            </p>
          ) : (
            <table>
              <thead>
                <tr>
                  <th>Name</th>
                  <th>Address</th>
                  <th>City</th>
                  <th>Postal Code</th>
                </tr>
              </thead>

              <tbody>
                {sites.map((site) => (
                  <tr key={site.id}>
                    <td>{site.name}</td>
                    <td>{site.address}</td>
                    <td>{site.city}</td>
                    <td>{site.postalCode}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>

      </div>
    </>
  );
}