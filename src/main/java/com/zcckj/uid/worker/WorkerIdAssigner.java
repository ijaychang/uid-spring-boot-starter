
package com.zcckj.uid.worker;

/**
 * Represents a worker id assigner for {@link com.zcckj.uid.impl.DefaultUidGenerator}
 * 
 * @author fsren
 */
public interface WorkerIdAssigner {

    /**
     * Assign worker id for {@link com.zcckj.uid.impl.DefaultUidGenerator}
     * 
     * @return assigned worker id
     */
    long assignWorkerId();

}
