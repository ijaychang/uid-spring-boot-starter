
package com.zcckj.uid;

import com.zcckj.uid.exception.UidGenerateException;

/**
 * 业务单号生成
 * Represents a unique id generator.
 *
 * @author zhangjie
 */
public interface BizidGenerator {


    /**
     * Get a unique ID
     *
     * @return UID
     * @throws UidGenerateException
     */
    String getUID() throws UidGenerateException;

    /**
     * Get a unique ID
     *
     * @param bizPrefix 业务前缀标识
     * @return UID
     * @throws UidGenerateException
     */
    String getUID(String bizPrefix) throws UidGenerateException;

    /**
     * Parse the UID into elements which are used to generate the UID. <br>
     * Such as timestamp & workerId & sequence...
     *
     * @param uid
     * @return Parsed info
     */
    String parseUID(String uid);
}
