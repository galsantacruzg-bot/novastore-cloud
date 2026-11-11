import { Component, OnInit, inject } from '@angular/core';
import { ApiStatusService } from '../../core/services/api-status.service';

@Component({
  selector: 'app-api-status',
  standalone: true,
  templateUrl: './api-status.html',
  styleUrls: ['./api-status.css']
})
export class ApiStatusComponent implements OnInit {
  readonly status = inject(ApiStatusService);
  ngOnInit(): void { this.status.check(); }
}
