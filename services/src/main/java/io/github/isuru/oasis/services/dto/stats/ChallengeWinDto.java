package io.github.isuru.oasis.services.dto.stats;

public class ChallengeWinDto extends ChallengeWinnerDto {

    private Integer challengeId;
    private String challengeName;
    private String challengeDisplayName;

    public Integer getChallengeId() {
        return challengeId;
    }

    public void setChallengeId(Integer challengeId) {
        this.challengeId = challengeId;
    }

    public String getChallengeName() {
        return challengeName;
    }

    public void setChallengeName(String challengeName) {
        this.challengeName = challengeName;
    }

    public String getChallengeDisplayName() {
        return challengeDisplayName;
    }

    public void setChallengeDisplayName(String challengeDisplayName) {
        this.challengeDisplayName = challengeDisplayName;
    }
}
