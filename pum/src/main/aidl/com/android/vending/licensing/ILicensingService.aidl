package com.android.vending.licensing;

import com.android.vending.licensing.ILicenseResultListener;

interface ILicensingService {
    oneway void checkLicense(long nonce, String packageName, ILicenseResultListener listener);
}
