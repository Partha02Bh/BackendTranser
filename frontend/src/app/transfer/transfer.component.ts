import { Component, OnInit } from '@angular/core';
import { ApiService } from '../api.service';
import { Router } from '@angular/router';


@Component({
    selector: 'app-transfer',
    templateUrl: './transfer.component.html',
    styleUrls: ['./transfer.component.css']
})
export class TransferComponent implements OnInit {
    targetId: number | null = null;
    amount: number | null = null;
    showSuccessModal = false;
    showConfirmationModal = false; // New state
    lastTransaction: any = null;
    user: any;
    isAdmin = false;

    constructor(private api: ApiService, private router: Router) { }

    ngOnInit(): void {
        this.user = this.api.getCurrentUser();
        if (this.api.isAdmin()) {
            this.router.navigate(['/owner']);
        }
    }

    logout() {
        localStorage.clear();
        this.router.navigate(['/']);
    }

    // Step 1: Show Confirmation
    sendMoney() {
        if (!this.targetId || !this.amount) {
            alert('Please enter Target ID and Amount');
            return;
        }
        this.showConfirmationModal = true;
    }

    // Step 2: Execute Transfer
    confirmTransfer() {
        this.showConfirmationModal = false; // Hide confirmation

        // Use the 3-argument version of transfer: transfer(sourceId, targetId, amount)
        this.api.transfer(this.user.accountId, this.targetId!, this.amount!).subscribe(
            (response) => {
                this.lastTransaction = { targetId: this.targetId, amount: this.amount };
                this.showSuccessModal = true;
                this.targetId = null;
                this.amount = null;
            },
            (error) => {
                alert('Transfer Failed: ' + (error.error?.message || 'Unknown error'));
            }
        );
    }

    cancelTransfer() {
        this.showConfirmationModal = false;
    }

    closeModal() {
        this.showSuccessModal = false;
    }
}
