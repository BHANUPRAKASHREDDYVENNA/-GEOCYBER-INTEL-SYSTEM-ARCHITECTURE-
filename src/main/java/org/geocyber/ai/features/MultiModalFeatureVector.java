package org.geocyber.ai.features;

import java.util.Arrays;

/**
 * Encapsulates the 5 distinct intelligence modalities for cross-attention modeling.
 */
public class MultiModalFeatureVector {

    private final double[] newsEmbedding;    // Modality 1: 64 dimensions (Geopolitical News NLP)
    private final double[] gdeltFeatures;     // Modality 2: 6 dimensions (CAMEO, Goldstein, Tone, Tension)
    private final double[] ttpFeatures;        // Modality 3: 30 dimensions (MITRE ATT&CK enterprise TTPs)
    private final double[] iocFeatures;        // Modality 4: 16 dimensions (Infrastructure, ASN, hash type, entropy)
    private final double[] temporalFeatures;   // Modality 5: 8 dimensions (Hour-of-day, Day-of-week, Duration, Offset)

    private final String groundTruthActor;    // Ground truth label for supervised training
    private final String campaignId;          // Campaign ID for campaign-grouped train/test split

    public MultiModalFeatureVector(double[] newsEmbedding, double[] gdeltFeatures,
                                  double[] ttpFeatures, double[] iocFeatures,
                                  double[] temporalFeatures, String groundTruthActor, String campaignId) {
        this.newsEmbedding = newsEmbedding != null ? newsEmbedding : new double[64];
        this.gdeltFeatures = gdeltFeatures != null ? gdeltFeatures : new double[6];
        this.ttpFeatures = ttpFeatures != null ? ttpFeatures : new double[30];
        this.iocFeatures = iocFeatures != null ? iocFeatures : new double[16];
        this.temporalFeatures = temporalFeatures != null ? temporalFeatures : new double[8];
        this.groundTruthActor = groundTruthActor;
        this.campaignId = campaignId;
    }

    public double[] getNewsEmbedding() { return newsEmbedding; }
    public double[] getGdeltFeatures() { return gdeltFeatures; }
    public double[] getTtpFeatures() { return ttpFeatures; }
    public double[] getIocFeatures() { return iocFeatures; }
    public double[] getTemporalFeatures() { return temporalFeatures; }
    public String getGroundTruthActor() { return groundTruthActor; }
    public String getCampaignId() { return campaignId; }

    /**
     * Flattens all modalities into a single concatenated dense feature vector (Total dimension: 64+6+30+16+8 = 124)
     */
    public double[] flatten() {
        double[] flat = new double[64 + 6 + 30 + 16 + 8];
        int pos = 0;
        System.arraycopy(newsEmbedding, 0, flat, pos, newsEmbedding.length);
        pos += newsEmbedding.length;
        System.arraycopy(gdeltFeatures, 0, flat, pos, gdeltFeatures.length);
        pos += gdeltFeatures.length;
        System.arraycopy(ttpFeatures, 0, flat, pos, ttpFeatures.length);
        pos += ttpFeatures.length;
        System.arraycopy(iocFeatures, 0, flat, pos, iocFeatures.length);
        pos += iocFeatures.length;
        System.arraycopy(temporalFeatures, 0, flat, pos, temporalFeatures.length);
        return flat;
    }

    /**
     * Creates an ablated copy with a specific modality zeroed out for ablation benchmarking.
     */
    public MultiModalFeatureVector ablate(boolean dropNews, boolean dropGdelt, boolean dropTtps, boolean dropIocs, boolean dropTemporal) {
        return new MultiModalFeatureVector(
                dropNews ? new double[64] : this.newsEmbedding.clone(),
                dropGdelt ? new double[6] : this.gdeltFeatures.clone(),
                dropTtps ? new double[30] : this.ttpFeatures.clone(),
                dropIocs ? new double[16] : this.iocFeatures.clone(),
                dropTemporal ? new double[8] : this.temporalFeatures.clone(),
                this.groundTruthActor,
                this.campaignId
        );
    }
}
