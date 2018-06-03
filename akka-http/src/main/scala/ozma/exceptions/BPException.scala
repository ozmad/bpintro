package ozma.exceptions

import ozma.exceptions.BPErrorCode.BPErrorCode

class BPException(errMsg: String, errCode: BPErrorCode, cause: Throwable = null)
  extends Exception(errMsg, cause)
