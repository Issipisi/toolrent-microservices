// src/components/TariffView.jsx - Versión simplificada
import { useEffect, useState } from "react";
import toolGroupService from "../services/toolGroup.service";
import { Button, Paper, TextField, Typography, Stack, MenuItem, Alert } from "@mui/material";

const TariffView = () => {
  const [groups, setGroups] = useState([]);
  const [selectedGroup, setSelectedGroup] = useState("");
  const [selectedGroupData, setSelectedGroupData] = useState(null);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState({ type: '', text: '' });

  useEffect(() => {
    loadGroups();
  }, []);

  const loadGroups = async () => {
    setLoading(true);
    try {
      const res = await toolGroupService.getAll();
      setGroups(res.data);
    } catch (error) {
      setMessage({ type: 'error', text: 'Error cargando grupos' });
    } finally {
      setLoading(false);
    }
  };

  const handleGroupSelect = async (groupId) => {
    setSelectedGroup(groupId);
    if (!groupId) {
      setSelectedGroupData(null);
      return;
    }
    
    try {
      const res = await toolGroupService.getToolGroup(groupId);
      setSelectedGroupData(res.data);
    } catch (error) {
      setMessage({ type: 'error', text: 'Error cargando datos del grupo' });
    }
  };

  const handleUpdateTariff = async () => {
    if (!selectedGroupData) return;
    
    try {
      await toolGroupService.updateTariff(
        selectedGroupData.id,
        selectedGroupData.dailyRentalRate,
        selectedGroupData.dailyFineRate
      );
      setMessage({ type: 'success', text: 'Tarifa actualizada correctamente' });
    } catch (error) {
      setMessage({ type: 'error', text: 'Error actualizando tarifa' });
    }
  };

  const handleUpdateReplacementValue = async () => {
    if (!selectedGroupData) return;
    
    try {
      await toolGroupService.updateReplacementValue(
        selectedGroupData.id,
        selectedGroupData.replacementValue
      );
      setMessage({ type: 'success', text: 'Valor de reposición actualizado' });
    } catch (error) {
      setMessage({ type: 'error', text: 'Error actualizando valor de reposición' });
    }
  };

  return (
    <Paper sx={{ p: 4, background: "#ffffff" }}>
      <Typography variant="h4" sx={{ mb: 3, color: "#6c63ff" }}>
        Configuración de Tarifas y Valores
      </Typography>

      {message.text && (
        <Alert severity={message.type} sx={{ mb: 2 }}>
          {message.text}
        </Alert>
      )}

      <Stack spacing={3} sx={{ width: "100%", maxWidth: 500, margin: "0 auto", display: "flex", flexDirection: "column"  }}>
        {/* Selector de grupo */}
        <TextField
          select
          label="Seleccionar Grupo de Herramientas"
          value={selectedGroup}
          onChange={(e) => handleGroupSelect(e.target.value)}
          disabled={loading}
        >
          <MenuItem value="">-- Seleccionar un grupo --</MenuItem>
          {groups.map((g) => (
            <MenuItem key={g.id} value={g.id}>
              {g.name} ({g.category})
            </MenuItem>
          ))}
        </TextField>

        {selectedGroupData && (
          <>
            {/* Tarifas */}
            <Typography variant="h6">Tarifas</Typography>
            <TextField
              label="Tarifa Diaria de Alquiler ($)"
              type="number"
              value={selectedGroupData.dailyRentalRate || ''}
              onChange={(e) => setSelectedGroupData({
                ...selectedGroupData,
                dailyRentalRate: e.target.value
              })}
              fullWidth
            />
            <TextField
              label="Tarifa Diaria de Multa ($)"
              type="number"
              value={selectedGroupData.dailyFineRate || ''}
              onChange={(e) => setSelectedGroupData({
                ...selectedGroupData,
                dailyFineRate: e.target.value
              })}
              fullWidth
            />
            <Button 
              variant="contained" 
              onClick={handleUpdateTariff}
              disabled={!selectedGroupData.dailyRentalRate || !selectedGroupData.dailyFineRate}
            >
              Actualizar Tarifas
            </Button>

            {/* Valor de reposición */}
            <Typography variant="h6" sx={{ mt: 3 }}>Valor de Reposición</Typography>
            <TextField
              label="Valor de Reposición ($)"
              type="number"
              value={selectedGroupData.replacementValue || ''}
              onChange={(e) => setSelectedGroupData({
                ...selectedGroupData,
                replacementValue: e.target.value
              })}
              fullWidth
            />
            <Button 
              variant="contained" 
              onClick={handleUpdateReplacementValue}
              disabled={!selectedGroupData.replacementValue}
            >
              Actualizar Valor de Reposición
            </Button>
          </>
        )}
      </Stack>
    </Paper>
  );
};

export default TariffView;