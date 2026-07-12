import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { CatalogService } from '../../shared/services/catalog.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-catalog',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './catalog.component.html'
})
export class CatalogComponent implements OnInit {
  private catalogService = inject(CatalogService);
  private router = inject(Router);
  
  tariffs: any[] = [];
  addons: any[] = [];
  loading = true;

  ngOnInit() {
    this.catalogService.getTariffs().subscribe({
      next: (res: any) => {
        this.tariffs = res;
        this.loading = false;
      },
      error: (err: any) => {
        console.error(err);
        this.loading = false;
      }
    });

    this.catalogService.getAddons().subscribe({
      next: (res: any) => {
        this.addons = res;
      },
      error: (err: any) => {
        console.error(err);
      }
    });
  }

  buyItem(item: any, type: string) {
    this.router.navigate(['/checkout'], { 
      queryParams: { 
        productId: item.code || item.id || item.name, 
        price: item.price,
        type: type
      } 
    });
  }
}
