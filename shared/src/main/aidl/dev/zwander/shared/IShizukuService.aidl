package dev.zwander.shared;

import dev.zwander.shared.data.VerifyResult;

interface IShizukuService {
    void destroy() = 16777114;
    List<VerifyResult> verifyLinks(int sdk, String packageName) = 1;
    List<VerifyResult> unverifyLinks(int sdk, String packageName) = 2;
}