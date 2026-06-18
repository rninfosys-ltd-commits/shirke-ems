export interface WireCuttingReport {
    id?: number;
    batchNo: string;
    plantName: string;
    shift?: string;
    cuttingDate: string | Date;

    mouldNo: number;
    size: string;
    ballTestMm: number;

    // Table Fields for Len 100
    qty100?: number;
    quantityTotal100?: number;
    breakage100?: number;
    netQty100?: number;

    // Table Fields for Len 150
    qty150?: number;
    quantityTotal150?: number;
    breakage150?: number;
    netQty150?: number;

    totalItem?: number;
    remark?: string;
    time: string;
    cuttingStartTime?: string;
    cycleTime?: string;

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
