export interface WireCuttingReport {

    id?: number;

    batchNo: string;
    plantName: string;
    cuttingDate: string | Date;

    mouldNo: number;
    size: string;
    ballTestMm: number;

    // Length 100 fields
    len100Qty?: number;
    len100TotalQty?: number;
    len100Breakage?: number;
    len100NetQty?: number;

    // Length 150 fields
    len150Qty?: number;
    len150TotalQty?: number;
    len150Breakage?: number;
    len150NetQty?: number;

    totalItem?: number;
    remark?: string;
    time: string;

    // approval
    approvalStage?: string;
    approvedByL1?: string;
    approvedByL2?: string;
    approvedByL3?: string;

    // system
    userId: number;
    branchId: number;
    orgId: number;

    createdDate?: string | Date;
}
