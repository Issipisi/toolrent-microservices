import { useEffect, useState } from "react";
import customerService from "../services/customer.service";
import {
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography,
} from "@mui/material";

const CustomerView = () => {
  const [customers, setCustomers] = useState([]);
  const [open, setOpen] = useState(false);
  const [form, setForm] = useState({ name: "", rut: "", phone: "", email: "" });

  const loadCustomers = async () => {
    const data = await customerService.getAll();
    setCustomers(data.data);
  };

  const handleRegister = async () => {
    try {
      await customerService.register({
        name: form.name,
        rut: form.rut,
        phone: form.phone,
        email: form.email
      });
      setOpen(false);
      setForm({ name: "", rut: "", phone: "", email: "" });
      loadCustomers();
    } catch (error) {
      console.error("Error al registrar cliente:", error);
      alert("Error al registrar cliente: " + error.response?.data || error.message);
    }
  };

  const handleChangeStatus = async (id, newStatus) => {
    try {
      await customerService.updateStatus(id, newStatus);
      loadCustomers();
    } catch (error) {
      console.error("Error al cambiar estado:", error);
      alert("Error al cambiar estado: " + error.response?.data || error.message);
    }
  };

  useEffect(() => {
    loadCustomers();
  }, []);

  return (
    <Paper sx={{ p: 4, background: "#ffffff" }}>
      <Typography variant="h4" sx={{ mb: 3, color: "#6c63ff" }}>
        Clientes
      </Typography>

      <Button
        variant="contained"
        sx={{ mb: 3, background: "#6c63ff", ":hover": { background: "#c77dff" } }}
        onClick={() => setOpen(true)}
      >
        Registrar Cliente
      </Button>

      <TableContainer>
        <Table>
          <TableHead sx={{ background: "#f5f0ff" }}>
            <TableRow>
              <TableCell sx={{ color: "#2e2e4e" }}>Nombre</TableCell>
              <TableCell sx={{ color: "#2e2e4e" }}>RUT</TableCell>
              <TableCell sx={{ color: "#2e2e4e" }}>Teléfono</TableCell>
              <TableCell sx={{ color: "#2e2e4e" }}>Email</TableCell>
              <TableCell sx={{ color: "#2e2e4e" }}>Estado</TableCell>
              <TableCell sx={{ color: "#2e2e4e" }}>Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {customers
              .filter((c) => c.email !== "system@toolrent.com")
              .map((c) => (
              <TableRow key={c.id} hover sx={{ "&:hover": { background: "#f5f0ff" } }}>
                <TableCell>{c.name}</TableCell>
                <TableCell>{c.rut}</TableCell>
                <TableCell>{c.phone}</TableCell>
                <TableCell>{c.email}</TableCell>
                <TableCell>{c.status}</TableCell>
                <TableCell>
                  <Button
                    size="small"
                    variant="outlined"
                    sx={{ color: "#9d4edd", borderColor: "#9d4edd" }}
                    onClick={() => handleChangeStatus(c.id, c.status === "ACTIVE" ? "RESTRICTED" : "ACTIVE")}
                  >
                    {c.status === "ACTIVE" ? "Restringir" : "Activar"}
                  </Button>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Modal registro */}
      <Dialog open={open} onClose={() => setOpen(false)}>
        <DialogTitle sx={{ background: "#f5f0ff", color: "#6c63ff" }}>Nuevo Cliente</DialogTitle>
        <DialogContent sx={{ display: "flex", flexDirection: "column", gap: 2, mt: 2 }}>
          <TextField label="Nombre" value={form.name} onChange={(e) => setForm({ ...form, name: e.target.value })} />
          <TextField label="RUT" value={form.rut} onChange={(e) => setForm({ ...form, rut: e.target.value })} />
          <TextField label="Teléfono" value={form.phone} onChange={(e) => setForm({ ...form, phone: e.target.value })} />
          <TextField label="Email" value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpen(false)} sx={{ color: "#4a4a68" }}>Cancelar</Button>
          <Button onClick={handleRegister} variant="contained" sx={{ background: "#6c63ff", ":hover": { background: "#c77dff" } }}>
            Guardar
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default CustomerView;