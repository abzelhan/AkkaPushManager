package kz.wg.push;

import java.util.List;

/**
 * Created by bakhyt on 2/14/18.
 */

//{"multicast_id":7264571237284315856,"success":0,"failure":1,"canonical_ids":0,"results":[{"error":"NotRegistered"}]}
public class FirebaseResult {
    Long multicast_id;
    int success;
    int failure;
    int canonical_ids;

    List<kz.wg.push.FirebaseResultMessages> results;

    public Long getMulticast_id() {
        return multicast_id;
    }

    public void setMulticast_id(Long multicast_id) {
        this.multicast_id = multicast_id;
    }

    public int getSuccess() {
        return success;
    }

    public void setSuccess(int success) {
        this.success = success;
    }

    public int getFailure() {
        return failure;
    }

    public void setFailure(int failure) {
        this.failure = failure;
    }

    public int getCanonical_ids() {
        return canonical_ids;
    }

    public void setCanonical_ids(int canonical_ids) {
        this.canonical_ids = canonical_ids;
    }

    public List<kz.wg.push.FirebaseResultMessages> getResults() {
        return results;
    }

    public void setResults(List<kz.wg.push.FirebaseResultMessages> results) {
        this.results = results;
    }
}
