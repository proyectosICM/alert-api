package com.icm.alert_api.controllers;

import com.icm.alert_api.dto.shift.CreateShiftRequest;
import com.icm.alert_api.dto.shift.ShiftDetailDto;
import com.icm.alert_api.dto.shift.ShiftSummaryDto;
import com.icm.alert_api.dto.shift.UpdateShiftRequest;
import com.icm.alert_api.services.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
@Validated
public class ShiftController {

    private final ShiftService shiftService;

    // =======================
    // (… tu controller actual)
    // =======================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ShiftDetailDto create(@Valid @RequestBody CreateShiftRequest request) {
        try {
            return shiftService.create(request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @GetMapping("/{id}")
    public ShiftDetailDto findById(@RequestParam("companyId") Long companyId, @PathVariable("id") Long id) {
        return shiftService.findById(companyId, id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Shift not found: " + id));
    }

    @GetMapping
    public Page<ShiftSummaryDto> listAll(@RequestParam("companyId") Long companyId, Pageable pageable) {
        return shiftService.listAll(companyId, pageable);
    }

    @GetMapping("/current")
    public List<ShiftSummaryDto> listCurrent(@RequestParam("companyId") Long companyId) {
        return shiftService.listCurrent(companyId);
    }

    @GetMapping("/date")
    public List<ShiftSummaryDto> listByDate(
            @RequestParam("companyId") Long companyId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return shiftService.listByDate(companyId, date);
    }

    @GetMapping("/date/page")
    public Page<ShiftSummaryDto> listByDatePaged(
            @RequestParam("companyId") Long companyId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable
    ) {
        return shiftService.listByDate(companyId, date, pageable);
    }

    @GetMapping("/range")
    public Page<ShiftSummaryDto> listByDateRange(
            @RequestParam("companyId") Long companyId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Pageable pageable
    ) {
        return shiftService.listByDateRange(companyId, from, to, pageable);
    }

    @GetMapping("/batch")
    public List<ShiftSummaryDto> listByBatch(
            @RequestParam("companyId") Long companyId,
            @RequestParam("batchId") String batchId
    ) {
        return shiftService.listByBatch(companyId, batchId);
    }

    @GetMapping("/search")
    public Page<ShiftSummaryDto> search(
            @RequestParam("companyId") Long companyId,
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "active", required = false) Boolean active,
            @RequestParam(value = "from", required = false) String from,
            @RequestParam(value = "to", required = false) String to,
            Pageable pageable
    ) {
        try {
            LocalDate fromDt = (from == null || from.isBlank()) ? null : LocalDate.parse(from);
            LocalDate toDt = (to == null || to.isBlank()) ? null : LocalDate.parse(to);
            return shiftService.search(companyId, q, active, fromDt, toDt, pageable);
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Formato de fecha inválido para 'from' o 'to'. Usa YYYY-MM-DD (ej: 2026-02-25)",
                    ex
            );
        }
    }

    @PatchMapping("/{id}")
    public ShiftDetailDto update(
            @RequestParam("companyId") Long companyId,
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateShiftRequest request
    ) {
        try {
            return shiftService.update(companyId, id, request);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@RequestParam("companyId") Long companyId, @PathVariable("id") Long id) {
        try {
            shiftService.deleteById(companyId, id);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, ex.getMessage(), ex);
        }
    }

    // ==========================
    // ✅ MULTIPART IMPORT EXCEL
    // ==========================

    /**
     * POST /api/shifts/import-excel?companyId=...&date=2026-02-25
     * Content-Type: multipart/form-data
     * body: file=<excel>
     */
    @PostMapping(value = "/import-excel", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<ShiftDetailDto> importExcel(
            @RequestParam("companyId") Long companyId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestPart("file") MultipartFile file
    ) {
        try {
            if (file == null || file.isEmpty()) {
                throw new IllegalArgumentException("file is required");
            }

            List<CreateShiftRequest> shifts = parseExcelToCreateRequests(companyId, date, file);
            if (shifts.isEmpty()) {
                throw new IllegalArgumentException("No shifts parsed from Excel");
            }

            // reemplaza current (active=true) por un batch nuevo
            return shiftService.replaceCurrentBatch(companyId, date, shifts);

        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        } catch (Exception ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Error leyendo Excel: " + ex.getMessage(), ex);
        }
    }

    // ==========================
    // Parser POI (2 formatos)
    // ==========================

    private static final Pattern DNI_PATTERN = Pattern.compile("^\\d{7,12}$"); // ajusta a tu realidad
    private static final Pattern PLATE_PATTERN = Pattern.compile("^[A-Z0-9-]{4,15}$");

    private List<CreateShiftRequest> parseExcelToCreateRequests(Long companyId, LocalDate date, MultipartFile file) throws Exception {
        try (InputStream in = file.getInputStream();
             Workbook wb = WorkbookFactory.create(in)) {

            Sheet sheet = wb.getNumberOfSheets() > 0 ? wb.getSheetAt(0) : null;
            if (sheet == null) return List.of();

            // detecta formato
            Row r0 = sheet.getRow(0);
            Row r1 = sheet.getRow(1);

            if (looksLikeRowFormat(r0)) {
                return parseRowFormat(companyId, date, sheet);
            }

            if (looksLikeTwoRowHeaderFormat(r1)) {
                return parseTwoRowHeaderFormat(companyId, date, sheet);
            }

            // fallback: header turno por columna, valores mezclados (heurística dni/placa)
            return parseSingleHeaderHeuristic(companyId, date, sheet);
        }
    }

    // ---------- Formato A: Turno | DNI | Placa ----------
    private boolean looksLikeRowFormat(Row header) {
        if (header == null) return false;
        String all = rowToLower(header);
        return all.contains("turno") && (all.contains("dni") || all.contains("documento")) && all.contains("plac");
    }

    private List<CreateShiftRequest> parseRowFormat(Long companyId, LocalDate date, Sheet sheet) {
        Row header = sheet.getRow(0);
        int last = header.getLastCellNum();

        int idxTurno = -1, idxDni = -1, idxPlaca = -1;

        for (int c = 0; c < last; c++) {
            String h = cellToString(header.getCell(c)).toLowerCase();
            if (h.contains("turno")) idxTurno = c;
            else if (h.contains("dni") || h.contains("documento")) idxDni = c;
            else if (h.contains("plac")) idxPlaca = c;
        }

        if (idxTurno < 0) throw new IllegalArgumentException("Excel: no se encontró columna 'Turno'");
        if (idxDni < 0 && idxPlaca < 0) throw new IllegalArgumentException("Excel: no se encontró columna DNI o Placa");

        Map<String, ShiftAccumulator> map = new LinkedHashMap<>();

        for (int r = 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            String turno = cellToString(row.getCell(idxTurno)).trim();
            if (turno.isBlank()) continue;

            ShiftAccumulator acc = map.computeIfAbsent(turno, k -> new ShiftAccumulator());

            if (idxDni >= 0) acc.addDnis(splitValues(cellToString(row.getCell(idxDni))));
            if (idxPlaca >= 0) acc.addPlates(splitValues(cellToString(row.getCell(idxPlaca))));
        }

        return buildRequests(companyId, date, map);
    }

    // ---------- Formato B: 2 headers (Turno arriba, DNI/PLACA abajo) ----------
    private boolean looksLikeTwoRowHeaderFormat(Row row1) {
        if (row1 == null) return false;
        String all = rowToLower(row1);
        return all.contains("dni") || all.contains("plac");
    }

    private List<CreateShiftRequest> parseTwoRowHeaderFormat(Long companyId, LocalDate date, Sheet sheet) {
        Row header0 = sheet.getRow(0);
        Row header1 = sheet.getRow(1);

        if (header0 == null || header1 == null) {
            throw new IllegalArgumentException("Excel: se requieren 2 filas de encabezado (Turno + DNI/PLACA)");
        }

        int lastCol = Math.max(header0.getLastCellNum(), header1.getLastCellNum());
        if (lastCol <= 0) return List.of();

        // Para merges: si header0 está vacío en una col, reusa el último turno visto
        String currentShift = null;

        Map<String, ShiftAccumulator> map = new LinkedHashMap<>();

        for (int r = 2; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            currentShift = null;

            for (int c = 0; c < lastCol; c++) {
                String shiftName = cellToString(header0.getCell(c)).trim();
                if (!shiftName.isBlank()) {
                    currentShift = shiftName;
                }
                if (currentShift == null || currentShift.isBlank()) continue;

                String kind = cellToString(header1.getCell(c)).trim().toLowerCase(); // dni/placa
                if (kind.isBlank()) continue;

                String cellVal = cellToString(row.getCell(c)).trim();
                if (cellVal.isBlank()) continue;

                ShiftAccumulator acc = map.computeIfAbsent(currentShift, k -> new ShiftAccumulator());

                if (kind.contains("dni") || kind.contains("documento")) {
                    acc.addDnis(splitValues(cellVal));
                } else if (kind.contains("plac")) {
                    acc.addPlates(splitValues(cellVal));
                }
            }
        }

        return buildRequests(companyId, date, map);
    }

    // ---------- Fallback: 1 header con turnos, y debajo valores (heurística dni/placa) ----------
    private List<CreateShiftRequest> parseSingleHeaderHeuristic(Long companyId, LocalDate date, Sheet sheet) {
        Row header = sheet.getRow(0);
        if (header == null) throw new IllegalArgumentException("Excel: no hay encabezados (fila 0)");

        int lastCol = header.getLastCellNum();
        if (lastCol <= 0) return List.of();

        List<String> shiftByCol = new ArrayList<>(Collections.nCopies(lastCol, null));
        String lastShift = null;

        for (int c = 0; c < lastCol; c++) {
            String h = cellToString(header.getCell(c)).trim();
            if (!h.isBlank()) lastShift = h;
            shiftByCol.set(c, lastShift);
        }

        Map<String, ShiftAccumulator> map = new LinkedHashMap<>();

        for (int r = 1; r <= sheet.getLastRowNum(); r++) {
            Row row = sheet.getRow(r);
            if (row == null) continue;

            for (int c = 0; c < lastCol; c++) {
                String shift = shiftByCol.get(c);
                if (shift == null || shift.isBlank()) continue;

                String val = cellToString(row.getCell(c)).trim();
                if (val.isBlank()) continue;

                ShiftAccumulator acc = map.computeIfAbsent(shift, k -> new ShiftAccumulator());

                for (String token : splitValues(val)) {
                    String t = token.trim();
                    if (t.isBlank()) continue;

                    String digits = t.replaceAll("\\D+", "");
                    if (DNI_PATTERN.matcher(digits).matches()) {
                        acc.addDnis(List.of(digits));
                    } else {
                        String plate = normalizePlate(t);
                        if (PLATE_PATTERN.matcher(plate).matches()) {
                            acc.addPlates(List.of(plate));
                        }
                    }
                }
            }
        }

        return buildRequests(companyId, date, map);
    }

    // ==========================
    // helpers
    // ==========================

    private List<CreateShiftRequest> buildRequests(Long companyId, LocalDate date, Map<String, ShiftAccumulator> map) {
        List<CreateShiftRequest> out = new ArrayList<>();
        for (var e : map.entrySet()) {
            String shiftName = e.getKey();
            ShiftAccumulator acc = e.getValue();

            if (shiftName == null || shiftName.isBlank()) continue;

            CreateShiftRequest req = new CreateShiftRequest();
            req.setCompanyId(companyId);
            req.setRosterDate(date);
            req.setShiftName(shiftName);
            req.setResponsibleDnis(new ArrayList<>(acc.dnis));
            req.setVehiclePlates(new ArrayList<>(acc.plates));
            out.add(req);
        }
        return out;
    }

    private static class ShiftAccumulator {
        final LinkedHashSet<String> dnis = new LinkedHashSet<>();
        final LinkedHashSet<String> plates = new LinkedHashSet<>();

        void addDnis(List<String> values) {
            for (String v : values) {
                if (v == null) continue;
                String x = v.trim();
                if (x.isBlank()) continue;
                String digits = x.replaceAll("\\D+", "");
                if (!digits.isBlank()) dnis.add(digits);
            }
        }

        void addPlates(List<String> values) {
            for (String v : values) {
                if (v == null) continue;
                String p = normalizePlate(v);
                if (!p.isBlank()) plates.add(p);
            }
        }
    }

    private static String normalizePlate(String s) {
        String x = s.trim().toUpperCase();
        x = x.replaceAll("\\s+", "");
        x = x.replace("-", "");
        return x;
    }

    private static List<String> splitValues(String s) {
        if (s == null) return List.of();
        // soporta: saltos de línea, coma, punto y coma, slash
        String[] parts = s.split("[\\n,;/]+");
        List<String> out = new ArrayList<>();
        for (String p : parts) {
            String t = p.trim();
            if (!t.isBlank()) out.add(t);
        }
        return out;
    }

    private static String cellToString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> {
                // evita "1234.0"
                double d = cell.getNumericCellValue();
                long l = (long) d;
                yield (d == l) ? Long.toString(l) : Double.toString(d);
            }
            case BOOLEAN -> Boolean.toString(cell.getBooleanCellValue());
            case FORMULA -> {
                try {
                    yield cell.getStringCellValue();
                } catch (Exception ignore) {
                    try {
                        double d = cell.getNumericCellValue();
                        long l = (long) d;
                        yield (d == l) ? Long.toString(l) : Double.toString(d);
                    } catch (Exception ignore2) {
                        yield "";
                    }
                }
            }
            default -> "";
        };
    }

    private static String rowToLower(Row row) {
        if (row == null) return "";
        StringBuilder sb = new StringBuilder();
        for (int c = 0; c < row.getLastCellNum(); c++) {
            sb.append(cellToString(row.getCell(c)).toLowerCase()).append(" ");
        }
        return sb.toString();
    }
}
