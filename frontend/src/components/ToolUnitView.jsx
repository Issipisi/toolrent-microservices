import React, { useEffect, useState } from "react";
import toolUnitService from "../services/toolUnit.service";
import {
  Button, Paper, Table, TableBody, TableCell, TableContainer, TableHead, TableRow,
  Typography, Stack, Dialog, DialogTitle, DialogContent, DialogActions,
  FormControl, InputLabel, Select, MenuItem,
} from "@mui/material";

const ToolUnitView = () => {
  const [units, setUnits] = useState([]);
  const [filtered, setFiltered] = useState([]);
  const [toolNames, setToolNames] = useState([]);        // nombres únicos
  const [selectedTool, setSelectedTool] = useState("Todas"); // opción por defecto
  const [openRepair, setOpenRepair] = useState(false);
  const [openRetire, setOpenRetire] = useState(false);
  const [openResolve, setOpenResolve] = useState(false);
  const [selectedUnit, setSelectedUnit] = useState(null);

  /* ---------- Carga inicial ---------- */
  const loadUnits = async () => {
    try {
      // Usar el endpoint correcto que ahora existe
      const res = await toolUnitService.getAll();
      console.log("Unidades cargadas:", res.data);
      setUnits(res.data || []);

      // Extrae nombres únicos y ordenados
      const names = Array.from(
        new Set(res.data.map(u => u.toolGroupName).filter(Boolean))
      ).sort();
      setToolNames(names);
    } catch (error) {
      console.error("Error cargando unidades:", error);
      alert("Error al cargar unidades: " + error.message);
      setUnits([]); // Inicializar como array vacío
    }
  };

  /* ---------- Filtro por herramienta seleccionada ---------- */
  useEffect(() => {
    if (selectedTool === "Todas") {
      setFiltered(units);
    } else {
      setFiltered(units.filter(u => u.toolGroupName === selectedTool));
    }
  }, [selectedTool, units]);

  /* ---------- Handlers de modales (sin cambios) ---------- */
  const handleSendToRepair = async () => {
    if (!selectedUnit) return;
    await toolUnitService.changeStatus(selectedUnit.id, "IN_REPAIR");
    setOpenRepair(false);
    loadUnits();
  };

  const handleRetire = async () => {
    if (!selectedUnit) return;
    await toolUnitService.changeStatus(selectedUnit.id, "RETIRED");
    setOpenRetire(false);
    loadUnits();
  };

  const handleResolveAvailable = async () => {
  if (!selectedUnit) return;
  try {
    await toolUnitService.changeStatus(selectedUnit.id, "AVAILABLE");
    setOpenResolve(false);
    loadUnits();
  } catch (error) {
    alert("Error marking as available: " + error.message);
  }
};

  const handleResolveRetire = async () => {
    if (!selectedUnit) return;
    await toolUnitService.changeStatus(selectedUnit.id, "RETIRED");
    setOpenResolve(false);
    loadUnits();
    window.dispatchEvent(new Event("debtUpdated"));
  };

  useEffect(() => { loadUnits(); }, []);

  return (
    <Paper sx={{ p: 4, background: "#ffffff" }}>
      <Typography variant="h4" sx={{ mb: 3, color: "#6c63ff" }}>Tool Units</Typography>

      {/* ---------- Selector de herramientas ---------- */}
      <FormControl fullWidth sx={{ mb: 3 }}>
        <InputLabel>Herramienta</InputLabel>
        <Select
          value={selectedTool}
          onChange={(e) => setSelectedTool(e.target.value)}
          label="Herramienta"
        >
          <MenuItem value="Todas">Todas</MenuItem>
          {toolNames.map((name) => (
            <MenuItem key={name} value={name}>
              {name}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      <TableContainer>
        <Table>
          <TableHead sx={{ background: "#f5f0ff" }}>
            <TableRow>
              <TableCell sx={{ color: "#2e2e4e" }}>ID</TableCell>
              <TableCell sx={{ color: "#2e2e4e" }}>Herramienta</TableCell>
              <TableCell sx={{ color: "#2e2e4e" }}>Estado</TableCell>
              <TableCell sx={{ color: "#2e2e4e" }}>Acciones</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {filtered.map((u) => (
              <TableRow key={u.id} hover sx={{ "&:hover": { background: "#f5f0ff" } }}>
                <TableCell>{u.id}</TableCell>
                <TableCell>
                  {/* Debugging: mostrar todas las propiedades disponibles */}
                  {u.toolGroupName || u.toolGroup?.name || u.name || "—"}
                </TableCell>
                <TableCell>{u.status}</TableCell>
                <TableCell>
                  <Stack direction="row" spacing={1}>
                    {u.status === "IN_REPAIR" && (
                      <Button
                        size="small"
                        variant="outlined"
                        onClick={() => { setSelectedUnit(u); setOpenResolve(true); }}
                      >
                        Resolver reparación
                      </Button>
                    )}

                    {u.status === "AVAILABLE" && (
                      <>
                        <Button
                          size="small"
                          variant="outlined"
                          onClick={() => { setSelectedUnit(u); setOpenRepair(true); }}
                        >
                          A Reparación
                        </Button>
                        <Button
                          size="small"
                          variant="outlined"
                          color="error"
                          onClick={() => { setSelectedUnit(u); setOpenRetire(true); }}
                        >
                          Retirar
                        </Button>
                      </>
                    )}
                  </Stack>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {/* ---------- Modales (sin cambios) ---------- */}
      <Dialog open={openRepair} onClose={() => setOpenRepair(false)} maxWidth="xs">
        <DialogTitle sx={{ background: "#f5f0ff", color: "#6c63ff" }}>A Reparación</DialogTitle>
        <DialogContent>
          <Typography>Unit ID: {selectedUnit?.id}</Typography>
          <Typography>Herramienta: {selectedUnit?.toolGroup?.name}</Typography>
          <Typography>El estado cambiará a "IN_REPAIR". Continuar?</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenRepair(false)}>Cancel</Button>
          <Button onClick={handleSendToRepair} variant="contained" color="warning">Confirm</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={openRetire} onClose={() => setOpenRetire(false)} maxWidth="xs">
        <DialogTitle sx={{ background: "#f5f0ff", color: "#6c63ff" }}>Retirar</DialogTitle>
        <DialogContent>
          <Typography>Unit ID: {selectedUnit?.id}</Typography>
          <Typography>Herramienta: {selectedUnit?.toolGroup?.name || "Sin nombre"}</Typography>
          <Typography>La Herramienta será "RETIRED". Continuar?</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenRetire(false)}>Cancel</Button>
          <Button onClick={handleRetire} variant="contained" color="error">Confirm</Button>
        </DialogActions>
      </Dialog>

      <Dialog open={openResolve} onClose={() => setOpenResolve(false)} maxWidth="xs">
        <DialogTitle sx={{ background: "#f5f0ff", color: "#6c63ff" }}>Resolver reparación</DialogTitle>
        <DialogContent>
          <Typography>Unit ID: {selectedUnit?.id}</Typography>
          <Typography>Herramienta: {selectedUnit?.toolGroup?.name}</Typography>
          <Typography sx={{ mt: 2 }}>¿Qué deseas hacer?</Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenResolve(false)}>Cancelar</Button>
          <Button onClick={handleResolveAvailable} variant="contained" color="success">
            Marcar como disponible
          </Button>
          <Button onClick={handleResolveRetire} variant="contained" color="error">
            Dar de baja definitiva
          </Button>
        </DialogActions>
      </Dialog>
    </Paper>
  );
};

export default ToolUnitView;