import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TicketService } from '../../shared/services/ticket.service';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';

@Component({
  selector: 'app-tickets',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './ticket.component.html'
})
export class TicketComponent implements OnInit {
  private ticketService = inject(TicketService);
  private fb = inject(FormBuilder);
  
  tickets: any[] = [];
  loading = true;
  showForm = false;

  ticketForm = this.fb.group({
    category: ['FAULT', Validators.required],
    priority: ['HIGH', Validators.required],
    description: ['', Validators.required]
  });

  ngOnInit() {
    this.loadTickets();
  }

  loadTickets() {
    this.loading = true;
    this.ticketService.getTickets().subscribe({
      next: (res: any) => {
        this.tickets = res;
        this.loading = false;
      },
      error: (err: any) => {
        console.error(err);
        this.loading = false;
      }
    });
  }

  onSubmit() {
    if (this.ticketForm.valid) {
      this.ticketService.createTicket(this.ticketForm.value as any).subscribe({
        next: () => {
          this.showForm = false;
          this.ticketForm.reset({ category: 'FAULT', priority: 'HIGH' });
          this.loadTickets();
        },
        error: (err: any) => console.error(err)
      });
    }
  }
}
