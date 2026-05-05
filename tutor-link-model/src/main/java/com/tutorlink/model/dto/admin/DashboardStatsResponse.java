package com.tutorlink.model.dto.admin;

import lombok.Data;

@Data
public class DashboardStatsResponse {

    private long totalUsers;
    private long totalTutors;
    private long totalOrders;
    private long totalRevenue;
    private long todayNewUsers;
    private long todayNewOrders;
    private long todayRevenue;
    private long pendingCertifications;
    private long activeOrders;
    private long completedOrders;
    private long totalPayments;
    private long totalReviews;
}
