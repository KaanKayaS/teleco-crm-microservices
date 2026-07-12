import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { OrderService } from '../../shared/services/order.service';

@Component({
  selector: 'app-checkout',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './checkout.component.html'
})
export class CheckoutComponent {
  private fb = inject(FormBuilder);
  private orderService = inject(OrderService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);

  loading = false;
  successMessage = '';

  checkoutForm = this.fb.group({
    productId: ['', Validators.required],
    quantity: [1, [Validators.required, Validators.min(1)]],
    totalAmount: ['', Validators.required],
    cardNumber: ['', Validators.required],
    cvv: ['', Validators.required],
    expiry: ['', Validators.required]
  });

  ngOnInit() {
    this.route.queryParams.subscribe(params => {
      if (params['productId']) {
        this.checkoutForm.patchValue({
          productId: params['productId'],
          totalAmount: params['price']
        });
      }
    });
  }

  onSubmit() {
    if (this.checkoutForm.valid) {
      this.loading = true;
      const val = this.checkoutForm.value;
      
      const orderRequest = {
        productId: val.productId,
        quantity: val.quantity,
        totalAmount: val.totalAmount
      };

      this.orderService.createOrder(orderRequest).subscribe({
        next: (orderRes: any) => {
          const paymentRequest = {
            orderId: orderRes.id,
            amount: val.totalAmount,
            paymentMethod: 'CREDIT_CARD',
            currency: 'TRY'
          };
          
          this.orderService.processPayment(paymentRequest).subscribe({
            next: () => {
              this.loading = false;
              this.successMessage = 'Order and payment completed successfully!';
              setTimeout(() => this.router.navigate(['/dashboard']), 3000);
            },
            error: (err: any) => {
              console.error(err);
              this.loading = false;
            }
          });
        },
        error: (err: any) => {
          console.error(err);
          this.loading = false;
        }
      });
    }
  }
}
