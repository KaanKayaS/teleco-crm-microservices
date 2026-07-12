import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DashboardService } from '../../shared/services/dashboard.service';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit {
  private dashboardService = inject(DashboardService);
  
  data: any = null;
  loading = true;
  error = '';

  ngOnInit() {
    this.loadDashboard();
  }

  loadDashboard() {
    // For demo purposes, we don't have a subscriptionId yet until they buy one.
    // We will just fetch with no subscriptionId. The BFF will return customer and invoices.
    this.dashboardService.getDashboardData().subscribe({
      next: (res: any) => {
        this.data = res;
        this.loading = false;
      },
      error: (err: any) => {
        this.error = 'Failed to load dashboard data.';
        this.loading = false;
        console.error(err);
      }
    });
  }
}
