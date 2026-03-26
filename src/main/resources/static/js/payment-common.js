/**
 * Common payment utilities shared across all payment pages.
 */
const PaymentCommon = {

    /**
     * Send the opaque payment data to our backend for processing.
     */
    processPayment: async function (opaqueData, amount, paymentMethod, billingInfo) {
        const payload = {
            opaqueDataDescriptor: opaqueData.dataDescriptor,
            opaqueDataValue: opaqueData.dataValue,
            amount: amount,
            paymentMethod: paymentMethod,
            firstName: billingInfo?.firstName || null,
            lastName: billingInfo?.lastName || null,
            email: billingInfo?.email || null
        };

        const response = await fetch('/api/payment/process', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });

        return await response.json();
    },

    /**
     * Show a status message on the page.
     */
    showStatus: function (elementId, message, type) {
        const el = document.getElementById(elementId);
        if (!el) return;
        el.className = 'alert alert-' + type;
        el.textContent = message;
        el.style.display = 'block';
    },

    hideStatus: function (elementId) {
        const el = document.getElementById(elementId);
        if (el) el.style.display = 'none';
    },

    showSpinner: function () {
        const el = document.getElementById('spinner');
        if (el) el.style.display = 'block';
    },

    hideSpinner: function () {
        const el = document.getElementById('spinner');
        if (el) el.style.display = 'none';
    },

    disableButton: function (btnId) {
        const btn = document.getElementById(btnId);
        if (btn) btn.disabled = true;
    },

    enableButton: function (btnId) {
        const btn = document.getElementById(btnId);
        if (btn) btn.disabled = false;
    },

    /**
     * Redirect to result page with query params.
     */
    redirectToResult: function (success, transactionId, message) {
        const params = new URLSearchParams({
            success: success,
            transactionId: transactionId || '',
            message: message || ''
        });
        window.location.href = '/payment/result?' + params.toString();
    }
};
