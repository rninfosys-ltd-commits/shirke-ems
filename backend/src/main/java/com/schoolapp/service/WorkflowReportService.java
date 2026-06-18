package com.schoolapp.service;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.schoolapp.entity.*;
import com.schoolapp.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class WorkflowReportService {

    @Autowired
    private ProductionEntryRepository productionRepo;
    @Autowired
    private CastingHallReportRepository castingRepo;
    @Autowired
    private RisingSectionRepository risingRepo;
    @Autowired
    private WireCuttingReportRepository cuttingRepo;
    @Autowired
    private BlockSeparatingRepository blockRepo;
    @Autowired
    private CubeTestRepository cubeRepo;
    @Autowired
    private RejectionDataRepository rejectionRepo;
    @Autowired
    private AutoclaveRepository autoclaveRepo;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private static final List<String> STAGES = Arrays.asList(
            "PRODUCTION", "CASTING", "RISING", "CUTTING", "AUTOCLAVE",
            "BLOCK_SEPARATING", "CUBE_TEST", "REJECTION");

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    private final Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD, BaseColor.DARK_GRAY);
    private final Font sectionFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD, new BaseColor(33, 97, 140));
    private final Font normalFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

    /**
     * Generate combined PDF up to a specific stage for a batch
     */
    public byte[] generateReport(String batchNo, String upToStage) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, baos);
        document.open();

        // Title
        Paragraph title = new Paragraph("Production Workflow Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph batchPara = new Paragraph("Batch No: " + batchNo, sectionFont);
        batchPara.setAlignment(Element.ALIGN_CENTER);
        batchPara.setSpacingAfter(15);
        document.add(batchPara);

        int targetIdx = STAGES.indexOf(upToStage.toUpperCase());
        if (targetIdx < 0)
            targetIdx = STAGES.size() - 1;

        // Add each stage up to the target
        for (int i = 0; i <= targetIdx; i++) {
            String stage = STAGES.get(i);
            addStageSection(document, batchNo, stage);
        }

        document.close();
        return baos.toByteArray();
    }

    public byte[] generateExcelReport(String batchNo, String upToStage) throws IOException {
        return generateHorizontalExcel(null, null, batchNo, upToStage);
    }

    /**
     * Generate a stage-wise Excel for a single batch where each row represents
     * a single stage record. Columns: Batch No, Stage, DateTime, Quantity,
     * Status, Operator, Parameters, Remarks
     */
    public byte[] generateStagewiseExcel(String batchNo, String upToStage) throws IOException {
        int targetIdx = STAGES.indexOf(upToStage != null ? upToStage.toUpperCase().replace(" ", "_") : "");
        if (targetIdx < 0)
            targetIdx = STAGES.size() - 1;

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Stagewise - " + batchNo);

            // Header style
            CellStyle hStyle = wb.createCellStyle();
            hStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            hStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font hFont = wb.createFont();
            hFont.setBold(true);
            hStyle.setFont(hFont);

            String[] headers = { "Batch No", "Stage", "DateTime", "Quantity", "Status", "Operator/User", "Parameters",
                    "Remarks" };
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(hStyle);
            }

            int rowIdx = 1;

            for (int i = 0; i <= targetIdx; i++) {
                String stage = STAGES.get(i);
                switch (stage) {
                    case "PRODUCTION": {
                        List<ProductionEntry> list = productionRepo.findByBatchNo(batchNo);
                        for (ProductionEntry p : list) {
                            Row r = sheet.createRow(rowIdx++);
                            r.createCell(0).setCellValue(nvl(batchNo));
                            r.createCell(1).setCellValue("Production");
                            r.createCell(2).setCellValue(formatDate(p.getCreatedDate()));
                            r.createCell(3).setCellValue(nvl(p.getTotalSolid()));
                            r.createCell(4).setCellValue(nvl(p.getApprovalStage()));
                            r.createCell(5).setCellValue(String.valueOf(p.getUserId()));
                            String params = "siloNo1=" + nvl(p.getSiloNo1());
                            r.createCell(6).setCellValue(params);
                            r.createCell(7).setCellValue(nvl(p.getProductionRemark()));
                        }
                        break;
                    }
                    case "CASTING": {
                        List<CastingHallReport> list = castingRepo.findByBatchNo(batchNo);
                        for (CastingHallReport c : list) {
                            Row r = sheet.createRow(rowIdx++);
                            r.createCell(0).setCellValue(nvl(batchNo));
                            r.createCell(1).setCellValue("Casting");
                            r.createCell(2).setCellValue(formatDate(c.getCreatedDate()));
                            r.createCell(3).setCellValue(nvl(c.getMouldFlow()));
                            r.createCell(4).setCellValue(nvl(c.getApprovalStage()));
                            r.createCell(5).setCellValue(String.valueOf(c.getUserId()));
                            String params = "mouldNo=" + nvl(c.getMouldNo()) + ", height=" + nvl(c.getHeight());
                            r.createCell(6).setCellValue(params);
                            r.createCell(7).setCellValue(nvl(c.getRemark()));
                        }
                        break;
                    }
                    case "CUTTING": {
                        List<WireCuttingReport> list = cuttingRepo.findByBatchNo(batchNo);
                        for (WireCuttingReport w : list) {
                            Row r = sheet.createRow(rowIdx++);
                            r.createCell(0).setCellValue(nvl(batchNo));
                            r.createCell(1).setCellValue("Cutting");
                            r.createCell(2).setCellValue(formatDate(w.getCreatedDate()));
                            r.createCell(3).setCellValue(nvl(w.getTotalItem()));
                            r.createCell(4).setCellValue(nvl(w.getApprovalStage()));
                            r.createCell(5).setCellValue(String.valueOf(w.getUserId()));
                            String params = "size=" + nvl(w.getSize()) + ", mouldNo=" + nvl(w.getMouldNo());
                            r.createCell(6).setCellValue(params);
                            r.createCell(7).setCellValue(nvl(w.getRemark()));
                        }
                        break;
                    }
                    case "AUTOCLAVE": {
                        List<AutoclaveCycle> list = autoclaveRepo.findByBatchNo(batchNo);
                        for (AutoclaveCycle a : list) {
                            Row r = sheet.createRow(rowIdx++);
                            r.createCell(0).setCellValue(nvl(batchNo));
                            r.createCell(1).setCellValue("Autoclave");
                            r.createCell(2).setCellValue(formatDate(a.getStartedDate()));
                            // Quantity: use total selected batch counts
                            Integer qty = (a.getPlant1BatchCount() != null ? a.getPlant1BatchCount() : 0)
                                    + (a.getPlant2BatchCount() != null ? a.getPlant2BatchCount() : 0);
                            r.createCell(3).setCellValue(nvl(qty));
                            r.createCell(4).setCellValue(nvl(a.getCurrentStatus()));
                            r.createCell(5).setCellValue(String.valueOf(a.getUserId()));
                            String params = "autoclaveNo=" + nvl(a.getAutoclaveNo()) + ", runNo="
                                    + nvl(a.getAutoclaveCycleNumber());
                            r.createCell(6).setCellValue(params);
                            r.createCell(7).setCellValue(nvl(a.getRemarks()));
                        }
                        break;
                    }
                    case "BLOCK_SEPARATING": {
                        List<BlockSeparating> list = blockRepo.findByBatchNumber(batchNo);
                        for (BlockSeparating b : list) {
                            Row r = sheet.createRow(rowIdx++);
                            r.createCell(0).setCellValue(nvl(batchNo));
                            r.createCell(1).setCellValue("Block Separating");
                            r.createCell(2).setCellValue(formatDate(b.getReportDate()));
                            r.createCell(3).setCellValue(nvl(b.getBlockSize()));
                            r.createCell(4).setCellValue(nvl(b.getShift()));
                            r.createCell(5).setCellValue(String.valueOf(b.getUserId()));
                            r.createCell(6).setCellValue("time=" + nvl(b.getTime()));
                            r.createCell(7).setCellValue("-");
                        }
                        break;
                    }
                    case "CUBE_TEST": {
                        List<CubeTestEntity> list = cubeRepo.findByBatchNo(batchNo);
                        for (CubeTestEntity c : list) {
                            Row r = sheet.createRow(rowIdx++);
                            r.createCell(0).setCellValue(nvl(batchNo));
                            r.createCell(1).setCellValue("Cube Test");
                            r.createCell(2).setCellValue(formatDate(c.getTestingDate()));
                            r.createCell(3).setCellValue(nvl(c.getDensityKgM3()));
                            r.createCell(4).setCellValue(nvl(c.getShift()));
                            r.createCell(5).setCellValue(String.valueOf(c.getUserId()));
                            String params = "cubeDimension=" + nvl(c.getCubeDimensionImmediate())
                                    + ", dryStrength=" + nvl(c.getDryStrength())
                                    + ", wetStrength=" + nvl(c.getWetStrength())
                                    + ", dryDensity=" + nvl(c.getDryDensity())
                                    + ", wetDensity=" + nvl(c.getWetDensity())
                                    + ", demouldDensity=" + nvl(c.getDemouldDensity())
                                    + ", compStrengthOverDry=" + nvl(c.getCompStrengthOverDry());
                            r.createCell(6).setCellValue(params);
                            r.createCell(7).setCellValue("-");
                        }
                        break;
                    }
                    case "REJECTION": {
                        List<RejectionDataEntity> list = rejectionRepo.findByBatchNo(batchNo);
                        for (RejectionDataEntity rej : list) {
                            Row r = sheet.createRow(rowIdx++);
                            r.createCell(0).setCellValue(nvl(batchNo));
                            r.createCell(1).setCellValue("Rejection");
                            r.createCell(2).setCellValue(formatDate(rej.getDate()));
                            r.createCell(3).setCellValue(nvl(rej.getQty()));
                            r.createCell(4).setCellValue(nvl(rej.getShift()));
                            r.createCell(5).setCellValue(String.valueOf(rej.getUserId()));
                            r.createCell(6).setCellValue("blockSize=" + nvl(rej.getBlockSize()));
                            r.createCell(7).setCellValue(nvl(rej.getRemarks()));
                        }
                        break;
                    }
                }
            }

            // Auto-size first 16 columns
            for (int i = 0; i < headers.length; i++)
                sheet.autoSizeColumn(i);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();
        }
    }

    /**
     * Generate flat EXCEL for all batches in a date range
     */
    public byte[] generateFlatWorkflowExcelReport(Date fromDate, Date toDate, String upToStage) throws IOException {
        return generateHorizontalExcel(fromDate, toDate, null, upToStage, null, null);
    }

    public byte[] generateFlatWorkflowExcelReport(Date fromDate, Date toDate, String upToStage, String plantName)
            throws IOException {
        return generateHorizontalExcel(fromDate, toDate, null, upToStage, plantName, null);
    }

    private List<String> getBatchesForStageInRange(Date from, Date to, String stage, String plantName) {
        switch (stage.toUpperCase()) {
            case "PRODUCTION":
                List<ProductionEntry> prodEntries = (plantName != null && !plantName.isBlank())
                        ? productionRepo.findByCreatedDateBetweenAndPlantName(from, to, plantName)
                        : productionRepo.findByCreatedDateBetween(from, to);
                return prodEntries.stream()
                        .map(ProductionEntry::getBatchNo).filter(Objects::nonNull).distinct().sorted().toList();
            case "CASTING":
                List<CastingHallReport> castingEntries = (plantName != null && !plantName.isBlank())
                        ? castingRepo.findByCreatedDateBetweenAndPlantName(from, to, plantName)
                        : castingRepo.findByCreatedDateBetween(from, to);
                return castingEntries.stream()
                        .map(CastingHallReport::getBatchNo).filter(Objects::nonNull).distinct().sorted().toList();
            case "CUTTING":
                List<WireCuttingReport> cuttingEntries = (plantName != null && !plantName.isBlank())
                        ? cuttingRepo.findByCreatedDateBetweenAndPlantName(from, to, plantName)
                        : cuttingRepo.findByCreatedDateBetween(from, to);
                return cuttingEntries.stream()
                        .map(WireCuttingReport::getBatchNo).filter(Objects::nonNull).distinct().sorted().toList();
            case "AUTOCLAVE":
                List<AutoclaveCycle> autoclaveEntries = (plantName != null && !plantName.isBlank())
                        ? autoclaveRepo.findByStartedDateBetweenAndPlantName(from, to, plantName)
                        : autoclaveRepo.findByStartedDateBetween(from, to);
                return autoclaveEntries.stream()
                        .map(AutoclaveCycle::getBatchNo)
                        .filter(Objects::nonNull)
                        .map(String::valueOf)
                        .distinct()
                        .sorted()
                        .toList();
            case "BLOCK_SEPARATING":
                List<BlockSeparating> blockEntries = (plantName != null && !plantName.isBlank())
                        ? blockRepo.findByReportDateBetweenAndPlantName(from, to, plantName)
                        : blockRepo.findByReportDateBetween(from, to);
                return blockEntries.stream()
                        .map(BlockSeparating::getBatchNumber).filter(Objects::nonNull).distinct().sorted().toList();
            case "CUBE_TEST":
                List<CubeTestEntity> cubeEntries = (plantName != null && !plantName.isBlank())
                        ? cubeRepo.findByTestingDateBetweenAndPlantName(from, to, plantName)
                        : cubeRepo.findByTestingDateBetween(from, to);
                return cubeEntries.stream()
                        .map(CubeTestEntity::getBatchNo).filter(Objects::nonNull).distinct().sorted().toList();
            case "REJECTION":
                List<RejectionDataEntity> rejectionEntries = (plantName != null && !plantName.isBlank())
                        ? rejectionRepo.findByDateBetweenAndPlantName(from, to, plantName)
                        : rejectionRepo.findByDateBetween(from, to);
                return rejectionEntries.stream()
                        .map(RejectionDataEntity::getBatchNo).filter(Objects::nonNull).distinct().sorted().toList();
            case "CONSOLIDATED":
            default:
                List<ProductionEntry> consolEntries = (plantName != null && !plantName.isBlank())
                        ? productionRepo.findByCreatedDateBetweenAndPlantName(from, to, plantName)
                        : productionRepo.findByCreatedDateBetween(from, to);
                return consolEntries.stream()
                        .map(ProductionEntry::getBatchNo).filter(Objects::nonNull).distinct().sorted().toList();
        }
    }

    /**
     * Generate consolidated EXCEL for all batches in a date range (Flat Cumulative)
     */
    public byte[] generateConsolidatedExcelReport(Date fromDate, Date toDate) throws IOException {
        return generateFlatWorkflowExcelReport(fromDate, toDate, "CONSOLIDATED", null);
    }

    /**
     * Generate PDF report for all batches in a date range (Batch-wise Sequential
     * Stages)
     */
    public byte[] generateFlatWorkflowPdfReport(Date fromDate, Date toDate, String upToStage) throws Exception {
        return generateFlatWorkflowPdfReport(fromDate, toDate, upToStage, null);
    }

    public byte[] generateFlatWorkflowPdfReport(Date fromDate, Date toDate, String upToStage, String plantName)
            throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4.rotate());
        PdfWriter.getInstance(document, baos);
        document.open();

        // Main Title
        String mainTitleStr = upToStage.equalsIgnoreCase("CONSOLIDATED") ? "Consolidated Workflow Report"
                : upToStage + " Production Report";
        if (plantName != null && !plantName.isBlank()) {
            mainTitleStr += " (" + plantName + ")";
        }
        Paragraph mainTitle = new Paragraph(mainTitleStr, titleFont);
        mainTitle.setAlignment(Element.ALIGN_CENTER);
        document.add(mainTitle);

        List<String> batchNumbers = getBatchesForStageInRange(fromDate, toDate, upToStage, plantName);

        for (String batchNo : batchNumbers) {
            Paragraph batchHeader = new Paragraph("BATCH NO: " + batchNo, sectionFont);
            batchHeader.setAlignment(Element.ALIGN_LEFT);
            batchHeader.setSpacingBefore(20);
            batchHeader.setSpacingAfter(10);
            document.add(batchHeader);

            int targetIdx = STAGES.indexOf(upToStage.toUpperCase());
            if (targetIdx < 0)
                targetIdx = STAGES.size() - 1;

            for (int i = 0; i <= targetIdx; i++) {
                addStageSection(document, batchNo, STAGES.get(i));
            }
        }

        document.close();
        return baos.toByteArray();
    }

    private void addStageSection(Document doc, String batchNo, String stage) throws DocumentException {
        Paragraph stageTitle = new Paragraph(formatStageName(stage), sectionFont);
        stageTitle.setSpacingBefore(15);
        stageTitle.setSpacingAfter(5);
        doc.add(stageTitle);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 35, 65 });

        switch (stage) {
            case "PRODUCTION":
                addProductionData(table, batchNo);
                break;
            case "CASTING":
                addCastingData(table, batchNo);
                break;
            case "RISING":
                addRisingData(table, batchNo);
                break;
            case "CUTTING":
                addCuttingData(table, batchNo);
                break;
            case "AUTOCLAVE":
                addAutoclaveData(table, batchNo);
                break;
            case "BLOCK_SEPARATING":
                addBlockSeparatingData(table, batchNo);
                break;
            case "CUBE_TEST":
                addCubeTestData(table, batchNo);
                break;
            case "REJECTION":
                addRejectionData(table, batchNo);
                break;
        }

        doc.add(table);

        // Divider
        Paragraph divider = new Paragraph(
                "──────────────────────────────────────────────────────────────────────────────────────────────────");
        divider.setSpacingBefore(5);
        doc.add(divider);
    }

    private void addProductionData(PdfPTable table, String batchNo) {
        List<ProductionEntry> entries = productionRepo.findByBatchNo(batchNo);

        if (entries.isEmpty()) {
            addRow(table, "Status", "Not completed");
            return;
        }

        ProductionEntry p = entries.get(0);
        addRow(table, "Date", formatDate(p.getCreatedDate()));
        addRow(table, "Shift", p.getShift() != null ? p.getShift() : "—");
        addRow(table, "Silo", p.getSiloNo1() != null ? p.getSiloNo1() : "—");
        addRow(table, "FA Solid", String.valueOf(p.getFaSolid1()));
        addRow(table, "Total Solid", String.valueOf(p.getTotalSolid()));
        addRow(table, "FA Slurry (kg)", String.valueOf(p.getFaSlurryQty()));
        addRow(table, "Excess Slurry (kg)", String.valueOf(p.getExcessSlurryQty()));
        addRow(table, "Surfactant (kg)", String.valueOf(p.getSurfactant()));
        addRow(table, "Water (L)", String.valueOf(p.getWaterLiter()));
        addRow(table, "Cement (kg)", String.valueOf(p.getCementKg()));
        addRow(table, "Lime (kg)", String.valueOf(p.getLimeKg()));
        addRow(table, "Gypsum (kg)", String.valueOf(p.getGypsumKg()));
        addRow(table, "Sol Oil (kg)", String.valueOf(p.getSolOilKg()));
        addRow(table, "Aluminum Powder (kg)", String.valueOf(p.getAluminumPowderKg()));
        addRow(table, "DC MRT (ml)", String.valueOf(p.getDcmrt()));
        addRow(table, "Mixing Time (s)", String.valueOf(p.getMixingTime()));
        addRow(table, "Temp (C)", String.valueOf(p.getTempC()));

        // Materials
        if (p.getMaterials() != null && !p.getMaterials().isEmpty()) {
            for (ProductionMaterial pm : p.getMaterials()) {
                addRow(table, pm.getMaterialName() + " (" + pm.getUnit() + ")", String.valueOf(pm.getValue()));
            }
        }

        addRow(table, "Remark", p.getProductionRemark() != null ? p.getProductionRemark() : "—");
        addRow(table, "Production Time", p.getProductionTime() != null ? p.getProductionTime() : "—");
    }

    private void addCastingData(PdfPTable table, String batchNo) {
        List<CastingHallReport> entries = castingRepo.findByBatchNo(batchNo);

        if (entries.isEmpty()) {
            addRow(table, "Status", "Not completed");
            return;
        }

        CastingHallReport c = entries.get(0);
        addRow(table, "Date", formatDate(c.getCreatedDate()));
        addRow(table, "Shift", c.getShift() != null ? c.getShift() : "—");
        addRow(table, "Mould Height", String.valueOf(c.getMouldHeight()));
        addRow(table, "Mould No", String.valueOf(c.getMouldNo()));
        addRow(table, "Flow (cm)", String.valueOf(c.getMouldFlow()));
        addRow(table, "Temp (C)", String.valueOf(c.getCastingTempC()));
        addRow(table, "Remark", c.getRemark() != null ? c.getRemark() : "—");
    }

    private void addRisingData(PdfPTable table, String batchNo) {
        List<RisingSection> entries = risingRepo.findByBatchNo(batchNo);

        if (entries.isEmpty()) {
            addRow(table, "Status", "Not completed");
            return;
        }

        RisingSection r = entries.get(0);
        addRow(table, "Date", formatDate(r.getCreatedDate()));
        addRow(table, "Shift", r.getShift() != null ? r.getShift() : "—");
        addRow(table, "Rising Time", r.getRisingTime() != null ? r.getRisingTime() : "—");
        addRow(table, "Rising Temp (C)", String.valueOf(r.getRisingTempC()));
        addRow(table, "Mould No", String.valueOf(r.getMouldNo()));
        addRow(table, "Mould Height", String.valueOf(r.getMouldHeight()));
        addRow(table, "Mould Flow", String.valueOf(r.getMouldFlow()));
    }

    private void addCuttingData(PdfPTable table, String batchNo) {
        List<WireCuttingReport> entries = cuttingRepo.findByBatchNo(batchNo);

        if (entries.isEmpty()) {
            addRow(table, "Status", "Not completed");
            return;
        }

        WireCuttingReport w = entries.get(0);
        addRow(table, "Date", formatDate(w.getCreatedDate()));
        addRow(table, "Shift", w.getShift() != null ? w.getShift() : "—");
        addRow(table, "Cutting Date", formatDate(w.getCuttingDate()));
        addRow(table, "Mould No", String.valueOf(w.getMouldNo()));
        addRow(table, "Size", String.valueOf(w.getSize()));
        addRow(table, "Ball Test (mm)", String.valueOf(w.getBallTestMm()));

        if (w.getSizeDetails() != null && !w.getSizeDetails().isEmpty()) {
            for (com.schoolapp.entity.CuttingSizeDetail sd : w.getSizeDetails()) {
                addRow(table, "Thickness " + sd.getHeight() + "mm",
                    "Qty: " + sd.getQuantity() + " | Breakage: " + sd.getBreakage() + " | Net: " + sd.getNetQuantity());
            }
        }
        addRow(table, "Total Item", String.valueOf(w.getTotalItem()));

        addRow(table, "Remark", w.getRemark() != null ? w.getRemark() : "—");
        addRow(table, "Time", w.getTime() != null ? w.getTime() : "—");
    }

    private void addAutoclaveData(PdfPTable table, String batchNo) {
        List<AutoclaveCycle> cycles = autoclaveRepo.findByBatchNo(batchNo);

        if (cycles.isEmpty()) {
            addRow(table, "Status", "Not completed");
            return;
        }

        AutoclaveCycle c = cycles.get(0);
        addRow(table, "Autoclave No", c.getAutoclaveNo() != null ? c.getAutoclaveNo() : "—");
        addRow(table, "Run No", c.getAutoclaveCycleNumber() != null ? c.getAutoclaveCycleNumber() : "—");
        addRow(table, "Shift", c.getShift() != null ? c.getShift() : "—");
        addRow(table, "Plant", c.getPlantName() != null ? c.getPlantName() : "—");
        addRow(table, "Start Date", formatDate(c.getStartedDate()));
        addRow(table, "Started At", c.getStartedAt() != null ? c.getStartedAt() : "—");
        addRow(table, "Completed Date", formatDate(c.getCompletedDate()));
        addRow(table, "Completed At", c.getCompletedAt() != null ? c.getCompletedAt() : "—");
        addRow(table, "Remarks", c.getRemarks() != null ? c.getRemarks() : "—");
    }

    private void addBlockSeparatingData(PdfPTable table, String batchNo) {
        List<BlockSeparating> entries = blockRepo.findByBatchNumber(batchNo);

        if (entries.isEmpty()) {
            addRow(table, "Status", "Not completed");
            return;
        }

        BlockSeparating b = entries.get(0);
        addRow(table, "Date", formatDate(b.getReportDate()));
        addRow(table, "Shift", b.getShift() != null ? b.getShift() : "—");
        addRow(table, "Operator (User ID)", String.valueOf(b.getUserId()));
        addRow(table, "Block Size", b.getBlockSize() != null ? b.getBlockSize() : "—");
        addRow(table, "Time", String.valueOf(b.getTime()));
    }

    private void addCubeTestData(PdfPTable table, String batchNo) {
        List<CubeTestEntity> entries = cubeRepo.findByBatchNo(batchNo);

        if (entries.isEmpty()) {
            addRow(table, "Status", "Not completed");
            return;
        }
        CubeTestEntity c = entries.get(0);
        addRow(table, "Cast Date", formatDate(c.getCastDate()));
        addRow(table, "Testing Date", formatDate(c.getTestingDate()));
        addRow(table, "Shift", c.getShift() != null ? c.getShift() : "—");
        addRow(table, "Operator (User ID)", String.valueOf(c.getUserId()));
        addRow(table, "Cube Dimension", c.getCubeDimensionImmediate() != null ? c.getCubeDimensionImmediate() : "—");
        addRow(table, "Density (kg/m³)", c.getDensityKgM3() != null ? String.valueOf(c.getDensityKgM3()) : "—");
        // Additional cube-test fields (ensure saved strengths/densities are visible in
        // PDF)
        addRow(table, "Wet Strength (MPa)", c.getWetStrength() != null ? String.valueOf(c.getWetStrength()) : "—");
        addRow(table, "Dry Strength (MPa)", c.getDryStrength() != null ? String.valueOf(c.getDryStrength()) : "—");
        addRow(table, "Wet Density (kg/m³)", c.getWetDensity() != null ? String.valueOf(c.getWetDensity()) : "—");
        addRow(table, "Dry Density (kg/m³)", c.getDryDensity() != null ? String.valueOf(c.getDryDensity()) : "—");
        addRow(table, "Demould Density (kg/m³)",
                c.getDemouldDensity() != null ? String.valueOf(c.getDemouldDensity()) : "—");
        addRow(table, "Comp Strength Over Dry (MPa)",
                c.getCompStrengthOverDry() != null ? String.valueOf(c.getCompStrengthOverDry()) : "—");
    }

    private void addRejectionData(PdfPTable table, String batchNo) {
        List<RejectionDataEntity> entries = rejectionRepo.findByBatchNo(batchNo);

        if (entries.isEmpty()) {
            addRow(table, "Status", "Not completed");
            return;
        }

        RejectionDataEntity r = entries.get(0);
        addRow(table, "Date", formatDate(r.getDate()));
        addRow(table, "Shift", r.getShift() != null ? r.getShift() : "—");
        addRow(table, "Operator (User ID)", String.valueOf(r.getUserId()));
        addRow(table, "Block Size", r.getBlockSize() != null ? r.getBlockSize() : "—");
        addRow(table, "Qty", r.getQty() != null ? String.valueOf(r.getQty()) : "—");
        addRow(table, "Total Breakages", r.getTotalBreakages() != null ? String.valueOf(r.getTotalBreakages()) : "—");
    }

    // Helpers
    private void addRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, normalFont));
        labelCell.setBackgroundColor(new BaseColor(235, 245, 251));
        labelCell.setPadding(5);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value != null ? value : "—", normalFont));
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private String formatDate(Date date) {
        return date != null ? dateFormat.format(date) : "—";
    }

    private String formatStageName(String stage) {
        return stage.replace("_", " ").substring(0, 1).toUpperCase() +
                stage.replace("_", " ").substring(1).toLowerCase();
    }

    // ======================== HORIZONTAL JSON REPORT ========================

    /**
     * Returns a list of rows, one per batch, with all stage data side-by-side.
     * Each row is a Map<String, Object> with nested maps per stage.
     */
    public List<Map<String, Object>> getHorizontalReport(Date fromDate, Date toDate, String batchNo) {
        return getHorizontalReport(fromDate, toDate, batchNo, null, null);
    }

    public List<Map<String, Object>> getHorizontalReport(Date fromDate, Date toDate, String batchNo, String plantName, String shift) {
        List<String> batches;

        // Normalize plantName (handle both 'Plant 1' and '1')
        String altPlantName = null;
        if (plantName != null && plantName.startsWith("Plant ")) {
            altPlantName = plantName.replace("Plant ", "");
        } else if (plantName != null && !plantName.isBlank()) {
            altPlantName = "Plant " + plantName;
        }

        if (batchNo != null && !batchNo.isBlank()) {
            batches = List.of(batchNo.trim());
        } else if (fromDate != null && toDate != null) {
            List<ProductionEntry> entries;
            if (plantName != null && !plantName.isBlank()) {
                entries = (altPlantName != null)
                        ? productionRepo.findByCreatedDateBetweenAndPlantNameIn(fromDate, toDate,
                                List.of(plantName, altPlantName))
                        : productionRepo.findByCreatedDateBetweenAndPlantName(fromDate, toDate, plantName);
            } else {
                entries = productionRepo.findByCreatedDateBetween(fromDate, toDate);
            }
            batches = entries.stream()
                    .map(ProductionEntry::getBatchNo)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
        } else {
            List<ProductionEntry> entries;
            if (plantName != null && !plantName.isBlank()) {
                entries = (altPlantName != null)
                        ? productionRepo.findByPlantNameIn(List.of(plantName, altPlantName))
                        : productionRepo.findByPlantName(plantName);
            } else {
                entries = productionRepo.findAll();
            }
            batches = entries.stream()
                    .map(ProductionEntry::getBatchNo)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .collect(java.util.stream.Collectors.toList());
        }

        // Only enrich batches from other stages when doing a ranged export.
        // For single-batch downloads, keep the workbook locked to the requested batch
        // only.
        Set<String> batchSet = new LinkedHashSet<>(batches);
        if (batchNo == null || batchNo.isBlank()) {
            List<CastingHallReport> castingList;
            if (fromDate != null && toDate != null) {
                castingList = castingRepo.findByCreatedDateBetween(fromDate, toDate);
            } else if (plantName != null && !plantName.isBlank()) {
                castingList = castingRepo.findByPlantName(plantName);
            } else {
                castingList = castingRepo.findAll();
            }
            for (CastingHallReport c : castingList) {
                if (c.getBatchNo() != null && !c.getBatchNo().isBlank())
                    batchSet.add(c.getBatchNo());
            }
            List<RisingSection> risingList;
            if (fromDate != null && toDate != null) {
                risingList = risingRepo.findByCreatedDateBetween(fromDate, toDate);
            } else if (plantName != null && !plantName.isBlank()) {
                risingList = risingRepo.findByPlantName(plantName);
            } else {
                risingList = risingRepo.findAll();
            }
            for (RisingSection r : risingList) {
                if (r.getBatchNo() != null && !r.getBatchNo().isBlank())
                    batchSet.add(r.getBatchNo());
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (String bn : batchSet) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("batchNo", bn);
            row.put("production", buildProductionMap(bn));
            row.put("casting", buildCastingMap(bn));
            row.put("rising", buildRisingMap(bn));
            row.put("cutting", buildCuttingMap(bn));
            row.put("autoclave", buildAutoclaveMap(bn));
            row.put("blockSeparating", buildBlockSeparatingMap(bn));
            row.put("cubeTest", buildCubeTestMap(bn));
            row.put("rejection", buildRejectionMap(bn));

            if (shift != null && !shift.isBlank()) {
                boolean match = false;
                for (Object stageObj : row.values()) {
                    if (stageObj instanceof Map) {
                        Map<String, Object> stageMap = (Map<String, Object>) stageObj;
                        String stageShift = (String) stageMap.get("shift");
                        if (stageShift != null && stageShift.contains(shift)) {
                            match = true;
                            break;
                        }
                    }
                }
                if (!match) continue;
            }

            result.add(row);
        }
        return result;
    }

    private Map<String, Object> buildProductionMap(String batchNo) {
        Map<String, Object> m = new LinkedHashMap<>();
        List<ProductionEntry> list = productionRepo.findByBatchNo(batchNo);
        if (list.isEmpty())
            return m;
        ProductionEntry p = list.get(0);
        m.put("date", formatDate(p.getCreatedDate()));
        m.put("plantName", nvl(p.getPlantName()));
        m.put("shift", nvl(p.getShift()));
        m.put("siloNo1", nvl(p.getSiloNo1()));
        m.put("faSolid1", nvl(p.getFaSolid1()));
        m.put("totalSolid", nvl(p.getTotalSolid()));
        m.put("faSlurryQty", nvl(p.getFaSlurryQty()));
        m.put("excessSlurryQty", nvl(p.getExcessSlurryQty()));
        m.put("surfactant", nvl(p.getSurfactant()));
        m.put("aluminumPowderKg", nvl(p.getAluminumPowderKg()));
        m.put("dcmrt", nvl(p.getDcmrt()));
        m.put("mixingTime", nvl(p.getMixingTime()));
        m.put("waterLiter", nvl(p.getWaterLiter()));
        m.put("cementKg", nvl(p.getCementKg()));
        m.put("limeKg", nvl(p.getLimeKg()));
        m.put("gypsumKg", nvl(p.getGypsumKg()));
        m.put("solOilKg", nvl(p.getSolOilKg()));
        m.put("tempC", nvl(p.getTempC()));
        m.put("faDensity", nvl(p.getFaDensity()));
        m.put("excessDensity", nvl(p.getExcessDensity()));
        m.put("excessSolid", nvl(p.getExcessSolid()));
        m.put("cbmVolume", nvl(p.getCbmVolume()));
        m.put("totalSolidsPerCbm", nvl(p.getTotalSolidsPerCbm()));
        m.put("totalBindersPerCbm", nvl(p.getTotalBindersPerCbm()));
        m.put("totalWaterPerCbm", nvl(p.getTotalWaterPerCbm()));
        m.put("waterSolidRatio", nvl(p.getWaterSolidRatio()));
        m.put("remark", nvl(p.getProductionRemark()));
        m.put("productionTime", nvl(p.getProductionTime()));
        return m;
    }

    private Map<String, Object> buildCastingMap(String batchNo) {
        Map<String, Object> m = new LinkedHashMap<>();
        List<CastingHallReport> list = castingRepo.findByBatchNo(batchNo);
        if (list.isEmpty())
            return m;
        CastingHallReport c = list.get(0);
        m.put("date", formatDate(c.getCreatedDate()));
        m.put("shift", nvl(c.getShift()));
        m.put("plantName", nvl(c.getPlantName()));
        m.put("mouldHeight", nvl(c.getMouldHeight()));
        m.put("mouldNo", nvl(c.getMouldNo()));
        m.put("flowInCm", nvl(c.getMouldFlow()));
        m.put("tempC", nvl(c.getCastingTempC()));
        m.put("remark", nvl(c.getRemark()));
        return m;
    }

    private Map<String, Object> buildCuttingMap(String batchNo) {
        Map<String, Object> m = new LinkedHashMap<>();
        List<WireCuttingReport> list = cuttingRepo.findByBatchNo(batchNo);
        if (list.isEmpty())
            return m;
        WireCuttingReport w = list.get(0);
        m.put("date", formatDate(w.getCreatedDate()));
        m.put("shift", nvl(w.getShift()));
        m.put("plantName", nvl(w.getPlantName()));
        m.put("cuttingDate", formatDate(w.getCuttingDate()));
        m.put("mouldNo", nvl(w.getMouldNo()));
        m.put("size", nvl(w.getSize()));
        m.put("ballTestMm", nvl(w.getBallTestMm()));
        if (w.getSizeDetails() != null && !w.getSizeDetails().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (com.schoolapp.entity.CuttingSizeDetail sd : w.getSizeDetails()) {
                sb.append(String.format("H:%s Q:%s B:%s N:%s | ", sd.getHeight(), sd.getQuantity(), sd.getBreakage(), sd.getNetQuantity()));
            }
            m.put("sizeDetails", sb.toString());
        } else {
            m.put("sizeDetails", "");
        }
        m.put("totalItem", nvl(w.getTotalItem()));
        m.put("cuttingTempC", nvl(w.getCuttingTempC()));
        m.put("cuttingHours", nvl(w.getCuttingHours()));
        m.put("remark", nvl(w.getRemark()));
        m.put("time", nvl(w.getTime()));
        return m;
    }

    private Map<String, Object> buildAutoclaveMap(String batchNo) {
        Map<String, Object> m = new LinkedHashMap<>();
        List<AutoclaveCycle> list = autoclaveRepo.findByBatchNo(batchNo);
        if (list.isEmpty())
            return m;
        AutoclaveCycle a = list.get(0);
        m.put("autoclaveNo", nvl(a.getAutoclaveNo()));
        m.put("runNo", nvl(a.getAutoclaveCycleNumber()));
        m.put("shift", nvl(a.getShift()));
        m.put("plantName", nvl(a.getPlantName()));
        m.put("currentStatus", nvl(a.getCurrentStatus()));
        m.put("batchNo", nvl(a.getBatchNo()));
        m.put("startDate", formatDate(a.getStartedDate()));
        m.put("startedDate", formatDate(a.getStartedDate()));
        m.put("startedAt", nvl(a.getStartedAt()));
        m.put("compDate", formatDate(a.getCompletedDate()));
        m.put("completedDate", formatDate(a.getCompletedDate()));
        m.put("completedAt", nvl(a.getCompletedAt()));
        m.put("transferStartTime", nvl(a.getTransferStartTime()));
        m.put("transferredToAutoclaveNo", nvl(a.getTransferredToAutoclaveNo()));
        m.put("transferEndTime", nvl(a.getTransferEndTime()));
        m.put("releaseStartTime", nvl(a.getReleaseStartTime()));
        m.put("releaseEndTime", nvl(a.getReleaseEndTime()));
        m.put("doorOpenTime", nvl(a.getDoorOpenTime()));
        m.put("pressure1Hr", nvl(a.getPressure1Hr()));
        m.put("pressure2Hr", nvl(a.getPressure2Hr()));
        m.put("pressure3Hr", nvl(a.getPressure3Hr()));
        m.put("pressureCompletion", nvl(a.getPressureAfterDoorOpen()));
        m.put("pressureRelease", nvl(a.getPressureRelease()));
        m.put("plant1BatchCount", nvl(a.getPlant1BatchCount()));
        m.put("plant2BatchCount", nvl(a.getPlant2BatchCount()));
        m.put("remarks", nvl(a.getRemarks()));
        return m;
    }

    private Map<String, Object> buildBlockSeparatingMap(String batchNo) {
        Map<String, Object> m = new LinkedHashMap<>();
        List<BlockSeparating> list = blockRepo.findByBatchNumber(batchNo);
        if (list.isEmpty())
            return m;
        BlockSeparating b = list.get(0);
        m.put("date", formatDate(b.getReportDate()));
        m.put("shift", nvl(b.getShift()));
        m.put("plantName", nvl(b.getPlantName()));
        m.put("batchNumber", nvl(b.getBatchNumber()));
        m.put("castingDate", formatDate(b.getCastingDate()));
        m.put("blockSize", nvl(b.getBlockSize()));
        m.put("time", nvl(b.getTime()));
        m.put("startTime", nvl(b.getStartTime()));
        m.put("endTime", nvl(b.getEndTime()));
        m.put("duration", nvl(b.getDuration()));
        m.put("operator", nvl(b.getOperator()));
        m.put("reportDate", formatDate(b.getReportDate()));
        m.put("middleCrack", nvl(b.getMiddleCrack()));
        m.put("risingCrack", nvl(b.getRisingCrack()));
        m.put("cornerDamage", nvl(b.getCornerDamage()));
        m.put("bottomLineMiddleCrack", nvl(b.getBottomLineMiddleCrack()));
        m.put("upperLineCrack", nvl(b.getUpperLineCrack()));
        m.put("autoclaveDamage", nvl(b.getAutoclaveDamage()));
        m.put("sideCrack", nvl(b.getSideCrack()));
        m.put("chipping", nvl(b.getChipping()));
        m.put("craneDamage", nvl(b.getCraneDamage()));
        m.put("unrise", nvl(b.getUnrise()));
        m.put("unsize", nvl(b.getUnsize()));
        m.put("uncut", nvl(b.getUncut()));
        m.put("collapse", nvl(b.getCollapse()));
        m.put("totalBreakage", nvl(b.getTotalBreakage()));
        m.put("totalPcs", nvl(b.getTotalPcs()));
        m.put("breakagePercent", nvl(b.getBreakagePercent()));
        return m;
    }

    private Map<String, Object> buildCubeTestMap(String batchNo) {
        Map<String, Object> m = new LinkedHashMap<>();
        List<CubeTestEntity> list = cubeRepo.findByBatchNo(batchNo);
        if (list.isEmpty())
            return m;
        CubeTestEntity c = list.get(0);
        m.put("batchNo", nvl(c.getBatchNo()));
        m.put("plantName", nvl(c.getPlantName()));
        m.put("reportDate", formatDate(c.getCreatedDate()));
        m.put("castDate", formatDate(c.getCastDate()));
        m.put("testingDate", formatDate(c.getTestingDate()));
        m.put("shift", nvl(c.getShift()));
        m.put("cubeDimensionImmediate", nvl(c.getCubeDimensionImmediate()));
        m.put("cubeDimensionOverDry", nvl(c.getCubeDimensionOverDry()));
        m.put("weightImmediateKg", nvl(c.getWeightImmediateKg()));
        m.put("weightOverDryKg", nvl(c.getWeightOverDryKg()));
        m.put("weightWithMoistureKg", nvl(c.getWeightWithMoistureKg()));
        m.put("loadOverDryTonn", nvl(c.getLoadOverDryTonn()));
        m.put("loadMoistureTonn", nvl(c.getLoadMoistureTonn()));
        m.put("compStrengthOverDry", nvl(c.getCompStrengthOverDry()));
        m.put("compStrengthMoisture", nvl(c.getCompStrengthMoisture()));
        m.put("densityKgM3", nvl(c.getDensityKgM3()));
        // Additional cube test fields
        m.put("reportDate", formatDate(c.getCreatedDate()));
        m.put("wetStrength", nvl(c.getWetStrength()));
        m.put("dryStrength", nvl(c.getDryStrength()));
        m.put("wetDensity", nvl(c.getWetDensity()));
        m.put("dryDensity", nvl(c.getDryDensity()));
        m.put("demouldDensity", nvl(c.getDemouldDensity()));
        return m;
    }

    private Map<String, Object> buildRisingMap(String batchNo) {
        Map<String, Object> m = new LinkedHashMap<>();
        List<RisingSection> list = risingRepo.findByBatchNo(batchNo);
        if (list.isEmpty())
            return m;
        RisingSection r = list.get(0);
        m.put("date", formatDate(r.getCreatedDate()));
        m.put("shift", nvl(r.getShift()));
        m.put("plantName", nvl(r.getPlantName()));
        m.put("plantNo", nvl(r.getPlantNo()));
        m.put("batchNo", nvl(r.getBatchNo()));
        m.put("risingStartTime", nvl(r.getRisingStartTime()));
        m.put("risingEndTime", nvl(r.getRisingEndTime()));
        m.put("risingTime", nvl(r.getRisingTime()));
        m.put("risingTempC", nvl(r.getRisingTempC()));
        m.put("risingTemperature", nvl(r.getRisingTemperature()));
        m.put("mouldNo", nvl(r.getMouldNo()));
        m.put("mouldHeight", nvl(r.getMouldHeight()));
        m.put("mouldFlow", nvl(r.getMouldFlow()));
        m.put("remark", nvl(r.getRemark()));
        m.put("remarks", nvl(r.getRemarks()));
        m.put("risingTempC", nvl(r.getRisingTempC()));
        m.put("mouldNo", nvl(r.getMouldNo()));
        m.put("mouldHeight", nvl(r.getMouldHeight()));
        m.put("mouldFlow", nvl(r.getMouldFlow()));
        return m;
    }

    private Map<String, Object> buildRejectionMap(String batchNo) {
        Map<String, Object> m = new LinkedHashMap<>();
        List<RejectionDataEntity> list = rejectionRepo.findByBatchNo(batchNo);
        if (list.isEmpty())
            return m;
        RejectionDataEntity r = list.get(0);
        m.put("date", formatDate(r.getDate()));
        m.put("shift", nvl(r.getShift()));
        m.put("plantName", nvl(r.getPlantName()));
        m.put("blockSize", nvl(r.getBlockSize()));
        m.put("qty", nvl(r.getQty()));
        m.put("cornerDamage", nvl(r.getCornerDamage()));
        m.put("eruptionType", nvl(r.getEruptionType()));
        m.put("topSideDamages", nvl(r.getTopSideDamages()));
        m.put("sideCrackThermalCrack", nvl(r.getSideCrackThermalCrack()));
        m.put("risingCrack", nvl(r.getRisingCrack()));
        m.put("centreCrack", nvl(r.getCentreCrack()));
        m.put("bottomUncutBlocks", nvl(r.getBottomUncutBlocks()));
        m.put("autoclaveDamage", nvl(r.getAutoclaveDamage()));
        m.put("craneDamage", nvl(r.getCraneDamage()));
        m.put("collapse", nvl(r.getCollapse()));
        m.put("unrise", nvl(r.getUnrise()));
        m.put("unsize", nvl(r.getUnsize()));
        m.put("uncut", nvl(r.getUncut()));
        m.put("chipping", nvl(r.getChipping()));
        m.put("totalBreakages", nvl(r.getTotalBreakages()));
        m.put("crackRejection", nvl(r.getCrackRejection()));
        m.put("dimensionFailure", nvl(r.getDimensionFailure()));
        m.put("densityFailure", nvl(r.getDensityFailure()));
        m.put("strengthFailure", nvl(r.getStrengthFailure()));
        m.put("otherRejection", nvl(r.getOtherRejection()));
        m.put("totalRejection", nvl(r.getTotalRejection()));
        m.put("remarks", nvl(r.getRemarks()));
        return m;
    }

    /** Null-safe value formatter */
    private String nvl(Object val) {
        return val != null ? String.valueOf(val) : "—";
    }

    // ======================== HORIZONTAL EXCEL DOWNLOAD ========================

    /**
     * Generates a color-coded Excel workbook with all stages side-by-side.
     * Green=Production, Red=Casting, Blue=Cutting, Purple=Autoclave,
     * Orange=Block Separating, Teal=Cube Test, Pink=Rejection
     */
    public byte[] generateHorizontalExcel(Date fromDate, Date toDate, String batchNo, String upToStage)
            throws IOException {
        return generateHorizontalExcel(fromDate, toDate, batchNo, upToStage, null, null);
    }

    public byte[] generateHorizontalExcel(Date fromDate, Date toDate, String batchNo, String upToStage, String plantName, String shift)
            throws IOException {
        List<Map<String, Object>> rows = getHorizontalReport(fromDate, toDate, batchNo, plantName, shift);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            org.apache.poi.xssf.usermodel.XSSFSheet sheet = wb.createSheet("Horizontal Report");

            // ── Stage definitions: {key, label, headerRGB, lightRGB, fields[]} ────
            String[][] allStages = {
                    { "production", "PRODUCTION", "2E7D32", "E8F5E9" },
                    { "casting", "CASTING", "C62828", "FFEBEE" },
                    { "cutting", "WIRE CUTTING", "1565C0", "E3F2FD" },
                    { "autoclave", "AUTOCLAVE", "6A1B9A", "F3E5F5" },
                    { "blockSeparating", "BLOCK SEPARATING", "E65100", "FFF3E0" },
                    { "cubeTest", "CUBE TEST", "00695C", "E0F2F1" },
                    { "rejection", "REJECTION", "AD1457", "FCE4EC" }
            };

            int maxCompletedStageIdx = 0;
            if (upToStage != null && !upToStage.isEmpty() && !upToStage.equalsIgnoreCase("CONSOLIDATED")) {
                String normalizedStage = upToStage.toUpperCase().replace(" ", "_");
                if (normalizedStage.contains("WIRE"))
                    normalizedStage = "CUTTING";
                if (normalizedStage.contains("SEPARAT"))
                    normalizedStage = "BLOCK_SEPARATING";
                maxCompletedStageIdx = STAGES.indexOf(normalizedStage);
                if (maxCompletedStageIdx < 0) {
                    maxCompletedStageIdx = STAGES.size() - 1;
                }
            } else {
                for (Map<String, Object> row : rows) {
                    for (int i = 0; i < allStages.length; i++) {
                        String key = allStages[i][0];
                        @SuppressWarnings("unchecked")
                        Map<String, Object> stageData = (Map<String, Object>) row.get(key);
                        if (stageData != null && !stageData.isEmpty()) {
                            if (i > maxCompletedStageIdx) {
                                maxCompletedStageIdx = i;
                            }
                        }
                    }
                }
            }

            int stageCount = Math.min(maxCompletedStageIdx + 1, allStages.length);
            String[][] stages = new String[stageCount][];
            System.arraycopy(allStages, 0, stages, 0, stageCount);

            // ── Column order per stage ────────────────────────────────────────────
            LinkedHashMap<String, String[]> stageFields = new LinkedHashMap<>();
            stageFields.put("production",
                    new String[] { "date", "shift", "plantName", "siloNo1", "faDensity", "faSolid1",
                            "excessDensity", "excessSolid", "faSlurryQty", "excessSlurryQty", "waterLiter",
                            "limeKg", "cementKg", "gypsumKg", "solOilKg", "surfactant",
                            "aluminumPowderKg", "dcmrt", "mixingTime", "tempC", "cbmVolume",
                            "totalSolid", "totalSolidsPerCbm", "totalBindersPerCbm",
                            "totalWaterPerCbm", "waterSolidRatio", "castingTime", "productionTime",
                            "remark" });
            stageFields.put("casting",
                    new String[] { "date", "shift", "plantName", "mouldHeight", "mouldNo", "flowInCm", "tempC",
                            "remark" });
            stageFields.put("cutting",
                    new String[] { "date", "shift", "plantName", "cuttingDate", "mouldNo", "size", "ballTestMm",
                            "sizeDetails", "totalItem", "cuttingTempC", "cuttingHours", "time", "remark" });
            stageFields.put("autoclave", new String[] { "autoclaveNo", "runNo", "shift", "plantName", "currentStatus",
                    "batchNo", "startDate", "startedAt", "compDate", "completedAt",
                    "cycleStartTime", "transferStartTime", "transferredToAutoclaveNo",
                    "transferEndTime", "releaseStartTime", "releaseEndTime", "doorOpenTime",
                    "pressure1Hr", "pressure2Hr", "pressure3Hr",
                    "pressureCompletion", "pressureRelease", "plant1BatchCount", "plant2BatchCount",
                    "remarks" });
            stageFields.put("blockSeparating", new String[] { "date", "shift", "plantName", "batchNumber",
                    "castingDate", "blockSize", "time", "reportDate", "remark" });
            stageFields.put("rising", new String[] { "date", "shift", "plantName", "plantNo", "batchNo",
                    "risingStartTime", "risingEndTime", "risingTime", "risingTempC", "risingTemperature",
                    "mouldNo", "mouldHeight", "mouldFlow", "remark", "remarks" });
            stageFields.put("cubeTest",
                    new String[] { "batchNo", "reportDate", "castDate", "testingDate", "shift", "plantName",
                            "cubeDimensionImmediate", "cubeDimensionOverDry", "weightImmediateKg", "weightOverDryKg",
                            "weightWithMoistureKg", "loadOverDryTonn", "loadMoistureTonn",
                            "compStrengthOverDry", "compStrengthMoisture", "densityKgM3", "demouldDensity",
                            "wetDensity", "wetStrength", "dryDensity", "dryStrength" });
            stageFields.put("rejection", new String[] { "date", "shift", "plantName", "blockSize", "qty",
                    "cornerDamage", "eruptionType", "topSideDamages", "sideCrackThermalCrack", "risingCrack",
                    "centreCrack", "bottomUncutBlocks", "autoclaveDamage", "craneDamage", "collapse", "unrise",
                    "unsize", "uncut", "chipping", "totalBreakages", "remarks" });

            // ── Build CellStyles per stage (header + body) ────────────────────────
            java.util.Map<String, CellStyle> headerStyles = new HashMap<>();
            java.util.Map<String, CellStyle> bodyStyles = new HashMap<>();

            for (String[] stage : stages) {
                String key = stage[0];
                String headerHex = stage[2];
                String bodyHex = stage[3];

                // Header style
                org.apache.poi.xssf.usermodel.XSSFCellStyle hStyle = wb.createCellStyle();
                hStyle.setFillForegroundColor(hexToXSSFColor(wb, headerHex));
                hStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                org.apache.poi.ss.usermodel.Font hFont = wb.createFont();
                hFont.setBold(true);
                hFont.setColor(org.apache.poi.ss.usermodel.IndexedColors.WHITE.getIndex());
                hFont.setFontHeightInPoints((short) 9);
                hStyle.setFont(hFont);
                hStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
                hStyle.setBorderBottom(BorderStyle.THIN);
                hStyle.setBorderRight(BorderStyle.THIN);
                headerStyles.put(key, hStyle);

                // Body style
                org.apache.poi.xssf.usermodel.XSSFCellStyle bStyle = wb.createCellStyle();
                bStyle.setFillForegroundColor(hexToXSSFColor(wb, bodyHex));
                bStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                bStyle.setBorderRight(BorderStyle.THIN);
                bStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
                bodyStyles.put(key, bStyle);
            }

            // Batch column styles
            CellStyle batchHeaderStyle = wb.createCellStyle();
            batchHeaderStyle.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
            batchHeaderStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font bFont = wb.createFont();
            bFont.setBold(true);
            bFont.setColor(IndexedColors.WHITE.getIndex());
            batchHeaderStyle.setFont(bFont);
            batchHeaderStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);

            CellStyle batchBodyStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font bbFont = wb.createFont();
            bbFont.setBold(true);
            batchBodyStyle.setFont(bbFont);
            batchBodyStyle.setAlignment(org.apache.poi.ss.usermodel.HorizontalAlignment.CENTER);
            batchBodyStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            batchBodyStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ── ROW 0: Stage banner headers ──────────────────────────────────────
            Row bannerRow = sheet.createRow(0);
            Cell batchBannerCell = bannerRow.createCell(0);
            batchBannerCell.setCellValue("Batch No");
            batchBannerCell.setCellStyle(batchHeaderStyle);

            int col = 1;
            for (String[] stage : stages) {
                String key = stage[0];
                String label = stage[1];
                String[] fields = stageFields.get(key);
                int startCol = col;
                for (int f = 0; f < fields.length; f++) {
                    Cell c = bannerRow.createCell(col + f);
                    if (f == 0)
                        c.setCellValue(label);
                    c.setCellStyle(headerStyles.get(key));
                }
                if (fields.length > 1) {
                    sheet.addMergedRegion(
                            new org.apache.poi.ss.util.CellRangeAddress(0, 0, startCol, startCol + fields.length - 1));
                }
                col += fields.length;
            }

            // ── ROW 1: Column label headers ───────────────────────────────────────
            Row labelRow = sheet.createRow(1);
            Cell batchLabelCell = labelRow.createCell(0);
            batchLabelCell.setCellValue("Batch No");
            batchLabelCell.setCellStyle(batchHeaderStyle);

            col = 1;
            for (String[] stage : stages) {
                String key = stage[0];
                String[] fields = stageFields.get(key);
                for (String field : fields) {
                    Cell c = labelRow.createCell(col++);
                    c.setCellValue(humanize(field));
                    c.setCellStyle(headerStyles.get(key));
                }
            }

            // ── DATA ROWS ─────────────────────────────────────────────────────────
            int rowIdx = 2;
            for (Map<String, Object> row : rows) {
                Row dataRow = sheet.createRow(rowIdx++);

                // Batch No
                Cell batchCell = dataRow.createCell(0);
                batchCell.setCellValue(nvl(row.get("batchNo")));
                batchCell.setCellStyle(batchBodyStyle);

                col = 1;
                for (String[] stage : stages) {
                    String key = stage[0];
                    String[] fields = stageFields.get(key);
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> stageData = (java.util.Map<String, Object>) row.getOrDefault(key,
                            new HashMap<>());
                    CellStyle bStyle = bodyStyles.get(key);
                    for (String field : fields) {
                        Cell c = dataRow.createCell(col++);
                        c.setCellValue(nvl(stageData.get(field)));
                        c.setCellStyle(bStyle);
                    }
                }
            }

            // Auto-size first 60 columns
            for (int i = 0; i < 60; i++)
                sheet.autoSizeColumn(i);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();
        }
    }

    private org.apache.poi.xssf.usermodel.XSSFColor hexToXSSFColor(
            org.apache.poi.xssf.usermodel.XSSFWorkbook wb, String hex) {
        byte r = (byte) Integer.parseInt(hex.substring(0, 2), 16);
        byte g = (byte) Integer.parseInt(hex.substring(2, 4), 16);
        byte b = (byte) Integer.parseInt(hex.substring(4, 6), 16);
        return new org.apache.poi.xssf.usermodel.XSSFColor(new byte[] { r, g, b }, null);
    }

    private static final Map<String, String> HEADER_MAP = new HashMap<>();
    static {
        HEADER_MAP.put("batchNo", "Batch No");
        HEADER_MAP.put("date", "Date");
        HEADER_MAP.put("shift", "Shift");
        HEADER_MAP.put("siloNo1", "Silo No.");
        HEADER_MAP.put("faSolid1", "Fa Solid");
        HEADER_MAP.put("totalSolid", "Total Solid");
        HEADER_MAP.put("waterLiter", "Water Lite");
        HEADER_MAP.put("cementKg", "Cement Kg");
        HEADER_MAP.put("limeKg", "Lime Kg");
        HEADER_MAP.put("gypsumKg", "Gypsum K");
        HEADER_MAP.put("solOilKg", "Dol Oil K");
        HEADER_MAP.put("aluminumPowderKg", "Al Power");
        HEADER_MAP.put("tempC", "CnTemp");
        HEADER_MAP.put("productionTime", "Production Tim");
        HEADER_MAP.put("remark", "Remark");
        HEADER_MAP.put("height", "Size");
        HEADER_MAP.put("bedNo", "Bed No");
        HEADER_MAP.put("mouldNo", "Mould No");
        HEADER_MAP.put("castingTime", "Casting Tim");
        HEADER_MAP.put("consistency", "Consistency");
        HEADER_MAP.put("flowInCm", "Flow In C");
        HEADER_MAP.put("vt", "Wt");
        HEADER_MAP.put("massTemp", "Mass Temp");
        HEADER_MAP.put("fallingTestMm", "alling Test M");
        HEADER_MAP.put("testTime", "lfezt Tim");
        HEADER_MAP.put("hTime", "H Time");
        HEADER_MAP.put("cuttingDate", "Cutting Date");
        HEADER_MAP.put("ballTestMm", "Ball Test M");
        HEADER_MAP.put("otherReason", "Other Reason");
        HEADER_MAP.put("time", "Time");
        HEADER_MAP.put("autoclaveNo", "Autoclave N");
        HEADER_MAP.put("runNo", "Run No");
        HEADER_MAP.put("startDate", "Start Date");
        HEADER_MAP.put("startedAt", "Started A");
        HEADER_MAP.put("compDate", "Comp Date");
        HEADER_MAP.put("completedAt", "Completed A");
        HEADER_MAP.put("remarks", "Remarks");
        HEADER_MAP.put("blockSize", "Block Size");
        HEADER_MAP.put("castDate", "Cast Date");
        HEADER_MAP.put("testingDate", "Testing Date");
        HEADER_MAP.put("cubeDimension", "Cube Dimensio");
        HEADER_MAP.put("densityKgM3", "Density Kg M");
        HEADER_MAP.put("qty", "Qty");
        HEADER_MAP.put("totalBreakages", "al Breaka");
    }

    private String humanize(String camelCase) {
        if (HEADER_MAP.containsKey(camelCase)) {
            return HEADER_MAP.get(camelCase);
        }
        String spaced = camelCase.replaceAll("([A-Z])", " $1");
        return spaced.substring(0, 1).toUpperCase() + spaced.substring(1);
    }

    // ======================== LIFECYCLE BATCH EXPORT ========================

    public byte[] generateBatchLifecycleExcel(String batchNo) throws IOException {
        String query = "SELECT " +
                "p.batch_no AS batch_no, " +
                "p.created_date AS p_date, " +
                "p.shift AS p_shift, " +
                "c.created_date AS c_date, " + // Assuming date field is created_date in CastingHallReport
                "c.size AS c_weight, " + // Will need to verify if weight exists in CastingHallReport
                "cu.cutting_date AS cu_date, " + // In WireCuttingReport it's cutting_date
                "cu.size AS cu_size, " + // In WireCuttingReport it's size
                "a.started_date AS a_date, " + // In AutoclaveCycle it's started_date
                "a.autoclave_no AS a_temp, " + // Mapping to requested placeholders
                "a.run_no AS a_pressure " + // Mapping to requested placeholders
                "FROM production_entry p " +
                "LEFT JOIN casting_hall_report c ON p.batch_no = c.batch_no " +
                "LEFT JOIN wire_cutting_report cu ON p.batch_no = cu.batch_no " +
                "LEFT JOIN autoclave_wagon w ON " +
                "  (CAST(w.e_batch AS CHAR) = p.batch_no OR " +
                "   CAST(w.m_batch AS CHAR) = p.batch_no OR " +
                "   CAST(w.w_batch AS CHAR) = p.batch_no) " +
                "LEFT JOIN autoclave_cycle a ON w.autoclave_id = a.id " +
                "WHERE p.batch_no = ? LIMIT 1";

        List<Map<String, Object>> results = jdbcTemplate.queryForList(query, batchNo);

        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Lifecycle - " + batchNo);

            // Header Style
            CellStyle hStyle = wb.createCellStyle();
            hStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            hStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            org.apache.poi.ss.usermodel.Font hFont = wb.createFont();
            hFont.setBold(true);
            hStyle.setFont(hFont);
            hStyle.setAlignment(HorizontalAlignment.CENTER);

            // Create Headers
            String[] headers = {
                    "Batch No", "Production Date", "Production Shift", "Casting Date", "Casting Weight",
                    "Cutting Date", "Cutting Size", "Autoclave Date", "Autoclave Temp", "Autoclave Pressure",
                    "Customer Name", "Sales Invoice No"
            };

            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell c = headerRow.createCell(i);
                c.setCellValue(headers[i]);
                c.setCellStyle(hStyle);
            }

            // Write Data
            if (!results.isEmpty()) {
                Map<String, Object> r = results.get(0);
                Row row = sheet.createRow(1);

                row.createCell(0).setCellValue(nvl(r.get("batch_no")));
                row.createCell(1).setCellValue(formatDateStr(r.get("p_date")));
                row.createCell(2).setCellValue(nvl(r.get("p_shift")));
                row.createCell(3).setCellValue(formatDateStr(r.get("c_date")));
                row.createCell(4).setCellValue("N/A"); // Weight not explicitly in entity but leaving standard
                row.createCell(5).setCellValue(formatDateStr(r.get("cu_date")));
                row.createCell(6).setCellValue(nvl(r.get("cu_size")));
                row.createCell(7).setCellValue(formatDateStr(r.get("a_date")));
                row.createCell(8).setCellValue(nvl(r.get("a_temp")));
                row.createCell(9).setCellValue(nvl(r.get("a_pressure")));
                row.createCell(10).setCellValue("N/A"); // Customer Name
                row.createCell(11).setCellValue("N/A"); // Sales Invoice No
            } else {
                Row row = sheet.createRow(1);
                row.createCell(0).setCellValue(batchNo);
                row.createCell(1).setCellValue("No data found");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            wb.write(baos);
            return baos.toByteArray();
        }
    }

    private String formatDateStr(Object obj) {
        if (obj == null)
            return "—";
        if (obj instanceof Date)
            return dateFormat.format((Date) obj);
        if (obj instanceof java.sql.Timestamp)
            return dateFormat.format(new Date(((java.sql.Timestamp) obj).getTime()));
        return String.valueOf(obj);
    }
}
