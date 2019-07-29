package group26;


import java.util.*;

import genius.core.AgentID;
import genius.core.Bid;
import genius.core.actions.Accept;
import genius.core.actions.Action;
import genius.core.actions.Offer;
import genius.core.issue.Issue;
import genius.core.issue.Value;
import genius.core.parties.AbstractNegotiationParty;
import genius.core.parties.NegotiationInfo;

/**
 * ExampleAgent returns the bid that maximizes its own utility for half of the negotiation session.
 * In the second half, it offers a random bid. It only accepts the bid on the table in this phase,
 * if the utility of the bid is higher than Example Agent's last bid.
 */
public class Agent26 extends AbstractNegotiationParty {
    private final String description = "Example Agent";

    private  List<Issue> issuesList = null;
    public negotatingInfo negotatingInfo; //ä¸€ä¸ªç±»æ�¥ä¿�å­˜æ¯”èµ›ä¿¡æ�¯ï¼Ÿ
    public bidSearch bidSearch;
//    public actionStrategy actionStrategy;

    private HashMap<Issue, HashMap<Value, Double>> biases = null;
    private HashMap<Issue, LinkedHashMap<Value, Double>> biasMap = null;

    private Bid lastReceivedOffer;
    private Bid myLastOffer;

    //æŽ¥å�—æ¶ˆæ�¯ç”¨å�‚æ•°
    double lastReceivedOfferBiases = -100.00;
    double maxReciverdOfferBiases  = -100.00;
    Bid maxReceviedOffer = null;

    //è¡ŒåŠ¨ç”¨å�‚æ•°
    double myLastOfferBiases = -99.00 ;
    private HashMap<Integer,Value> newbidvalue;   //ä¸´æ—¶valueå�‚æ•°

    @Override
    public void init(NegotiationInfo info) {
        super.init(info);

        System.out.println("ä»£ç�†åˆ�å§‹åŒ–å¼€å§‹");
        issuesList = info.getUserModel().getDomain().getIssues();
        negotatingInfo = new negotatingInfo(info);
        bidSearch = new bidSearch(info);

        biases = bidSearch.getBiases();
        System.out.println("æµ‹è¯•å��å·®å€¼è°ƒç”¨æ˜¯å�¦æˆ�åŠŸï¼š");
        System.out.println(biases);

        biasMap = bidSearch.getBiasMap();


        System.out.println("ä»£ç�†åˆ�å§‹åŒ–ç»“æ�Ÿ");
    }

    /**
     * When this function is called, it is expected that the Party chooses one of the actions from the possible
     * action list and returns an instance of the chosen action.
     *
     * @param list
     * @return
     */
    @Override
    public Action chooseAction(List<Class<? extends Action>> list) {
        double time = getTimeLine().getTime();


        if (time < 0.05){
            System.out.println("å¼€å§‹æ‰¾åˆ°å¤©å‘½");
            myLastOffer = getMaxUtilityBid();
            myLastOfferBiases = getBidBiases(myLastOffer);
            if (lastReceivedOffer != null && myLastOfferBiases < lastReceivedOfferBiases){
                return new Accept(this.getPartyId(), lastReceivedOffer);
            }else {
                return new Offer(this.getPartyId(), myLastOffer);
            }
        }

        if (time >= 0.05 && time < 0.7){
            if (myLastOffer != null && lastReceivedOffer != null && getBidBiases(myLastOffer)* 0.95 < getBidBiases(lastReceivedOffer)){
                return  new  Accept(this.getPartyId(), lastReceivedOffer);
            }
            Bid finalbid = getMinConcession();
            return new Offer(this.getPartyId(), finalbid);
        }

        if (time >= 0.7 && time < 0.95){
            if (myLastOffer != null && lastReceivedOffer != null && getBidBiases(myLastOffer)* 0.95 < getBidBiases(lastReceivedOffer)){
                return  new  Accept(this.getPartyId(), lastReceivedOffer);
            }
            Bid finalbid = getMaxConcession();
            return new Offer(this.getPartyId(), finalbid);
        }


        if (time >= 0.98 && time < 0.999){
            if (myLastOffer != null && lastReceivedOffer != null && 0.3 < getBidBiases(lastReceivedOffer)){
                return  new  Accept(this.getPartyId(), lastReceivedOffer);
            }
            System.out.println("æŽ¥å�—åŽ†å�²æœ€ä½³");
            if (maxReciverdOfferBiases > lastReceivedOfferBiases && maxReciverdOfferBiases > 0.3){
                return  new Offer(this.getPartyId(), maxReceviedOffer);
            }
            Bid finalbid = getMaxConcession();
            if (getBidBiases(finalbid) > 0.3){
                return  new Offer(this.getPartyId(), maxReceviedOffer);
            }else {
                return  new Offer(this.getPartyId(), myLastOffer);
            }

        }

        if (time >= 0.999){
            if (lastReceivedOffer != null){
                System.out.println("æ…Œä¸�æ‹©è·¯æŽ¥å�—");
                return new Accept(this.getPartyId(), lastReceivedOffer);
            }
        }


        if (time < 0.5) {
            return new Offer(this.getPartyId(), this.getMaxUtilityBid());
        } else {

            if (lastReceivedOffer != null
                    && myLastOffer != null
                    && this.utilitySpace.getUtility(lastReceivedOffer) > this.utilitySpace.getUtility(myLastOffer)) {

                return new Accept(this.getPartyId(), lastReceivedOffer);
            } else {
                myLastOffer = generateRandomBid();
                return new Offer(this.getPartyId(), myLastOffer);
            }
        }
    }

    /**
     * This method is called to inform the party that another NegotiationParty chose an Action.
     * @param sender
     * @param act
     */
    @Override
    public void receiveMessage(AgentID sender, Action act) {
        super.receiveMessage(sender, act);

        if (act instanceof Offer) {
            System.out.println("ä»£ç�† " + sender + "æ”¶åˆ°æŠ¥ä»·");
            Offer offer = (Offer) act;
            lastReceivedOffer = offer.getBid();
            System.out.println("å¾—åˆ°æŠ¥ä»·ï¼š" + lastReceivedOffer);
            System.out.println("è¿›å…¥è®°å½•çŠ¶æ€�");
            Double utilitySum = 0.0;
            System.out.println("è®¡ç®—æ­¤æŠ¥ä»·å¯¹äºŽä»£ç�†çš„å��å·®å€¼");
            for (Issue issue: issuesList){
                //System.out.println("ä¸€æ¬¡å°�è¯•");
                Value issueValue = lastReceivedOffer.getValue(issue.getNumber());
                System.out.println("è¿™ä¸ªæŠ¥ä»·åœ¨ " + issue +  "ä¸Šçš„å€¼ä¸ºï¼š" + issueValue );
                if (biases.get(issue).containsKey(issueValue)){
                    utilitySum = utilitySum + biases.get(issue).get(issueValue);
                }else {
                    biases.get(issue).put(issueValue, 0.0);
                }
            }
            lastReceivedOfferBiases = utilitySum;
            System.out.println("æ­¤æŠ¥ä»·å¯¹äºŽä»£ç�†çš„å��å·®å€¼ï¼š" + utilitySum);
            negotatingInfo.storeGetOffer(lastReceivedOffer, utilitySum);

            if (utilitySum > maxReciverdOfferBiases){
                maxReciverdOfferBiases = utilitySum;
                maxReceviedOffer = lastReceivedOffer;
            }
        }
    }

    /**
     * A human-readable description for this party.
     * @return
     */
    @Override
    public String getDescription() {
        return description;
    }

    private Bid getMaxUtilityBid() {
        try {
            return this.utilitySpace.getMaxUtilityBid();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private Double getBidBiases(Bid myLastOffer) {
        double biasesSum = 0.0;
        for (Issue issue: issuesList){
            //System.out.println("ä¸€æ¬¡å°�è¯•");
            Value issueValue = myLastOffer.getValue(issue.getNumber());
            System.out.println("è¿™ä¸ªæŠ¥ä»·åœ¨ " + issue +  "ä¸Šçš„å€¼ä¸ºï¼š" + issueValue );
            biasesSum = biasesSum + biases.get(issue).get(issueValue);
        }
        return  biasesSum;
    }

    private Bid getMinConcession() {
        newbidvalue = new HashMap<Integer, Value>();
        List<Issue> issueListOfOp = lastReceivedOffer.getIssues();
        List<Issue> myIssueList = myLastOffer.getIssues();
        Value issueValue = null;
        System.out.println("BiasMap");
        System.out.println(biasMap);
        System.out.println("lastReceivedOffer");
        System.out.println(lastReceivedOffer);
        System.out.println("myLastOffer");
        System.out.println(myLastOffer);
        int IssueNum = 0;
        Value minValue = null;
        double minBias = 1000000000;
        for (Issue issue : issueListOfOp) {
            double biasofR;
            double biasofO;
            issueValue = lastReceivedOffer.getValue(issue.getNumber());
            LinkedHashMap<Value, Double> indexofValueInOur = biasMap.get(issue);
            if (indexofValueInOur.get(myLastOffer.getValue(issue.getNumber())) == null) {
                biasofO = 1000000000;
            } else {
                biasofO = indexofValueInOur.get(myLastOffer.getValue(issue.getNumber()));
            }
            if (indexofValueInOur.get(issueValue) == null) {
                biasofR = 0;
            } else {
                biasofR = indexofValueInOur.get(issueValue);
            }

            double difference = biasofO - biasofR;
            if (minBias > difference) {
                minBias = difference;
                minValue = issueValue;
                IssueNum = issue.getNumber();
            }
            System.out.println("=========min=========");
            System.out.println(minValue);
        }

        for (Issue issue : myIssueList) {
            Value myissuevalue = myLastOffer.getValue(issue.getNumber());
            //newbidvalue.put(issueNum, myissuevalue);
            if (issue.getNumber() == IssueNum) {
                newbidvalue.put(issue.getNumber(), minValue);
            } else {
                issueValue = myLastOffer.getValue(issue.getNumber());
                newbidvalue.put(issue.getNumber(), issueValue);
            }
        }
        Bid finalBid = new Bid(userModel.getDomain(), newbidvalue);
        System.out.println("=========newbid=========");
        System.out.println(finalBid);
        System.out.println("=========newbid=========");
        return finalBid;
    }


    private Bid getMaxConcession() {
        newbidvalue = new HashMap<Integer, Value>();
        List<Issue> issueListOfOp = lastReceivedOffer.getIssues();
        List<Issue> myIssueList = myLastOffer.getIssues();
        Value issueValue = null;
        System.out.println("BiasMap");
        System.out.println(biasMap);
        System.out.println("lastReceivedOffer");
        System.out.println(lastReceivedOffer);
        System.out.println("myLastOffer");
        System.out.println(myLastOffer);
        int IssueNum = 0;
        Value maxValue = null;
        double maxBias = -10000;
        for (Issue issue : issueListOfOp) {
            double biasofR;
            double biasofO;
            issueValue = lastReceivedOffer.getValue(issue.getNumber());
            LinkedHashMap<Value, Double> indexofValueInOur = biasMap.get(issue);
            if (indexofValueInOur.get(myLastOffer.getValue(issue.getNumber())) == null) {
                biasofO = -100000;
            } else {
                biasofO = indexofValueInOur.get(myLastOffer.getValue(issue.getNumber()));
            }
            if (indexofValueInOur.get(issueValue) == null) {
                biasofR = -10000;
            } else {
                biasofR = indexofValueInOur.get(issueValue);
            }
            double difference = biasofR - biasofO;
            if (maxBias < difference) {
                maxBias = difference;
                maxValue = issueValue;
                IssueNum = issue.getNumber();
            }
            System.out.println("=========max=========");
            System.out.println(maxValue);
        }
        for (Issue issue : issueListOfOp) {
            int issueNum = issue.getNumber();
            Value hisissuevalue = lastReceivedOffer.getValue(issue.getNumber());
            newbidvalue.put(issueNum, hisissuevalue);

        }
        for (Issue issue : issueListOfOp) {
            Value hisissuevalue = lastReceivedOffer.getValue(issue.getNumber());
            // newbidvalue.put(issueNum, hisissuevalue);
            if (issue.getNumber() == IssueNum) {
                newbidvalue.put(issue.getNumber(), maxValue);
            } else {
                issueValue = lastReceivedOffer.getValue(issue.getNumber());
                newbidvalue.put(issue.getNumber(), hisissuevalue);
            }
        }
        Bid finalBid = new Bid(userModel.getDomain(), newbidvalue);
        System.out.println("=========newbid=========");
        System.out.println(finalBid);
        System.out.println("=========newbid=========");
        return finalBid;
    }
}











class negotatingInfo {

    private List<Issue> issuesList;                                          //è¿™ä¸ªè°ˆåˆ¤çš„issue
    private HashMap<Issue, HashMap<Value, Double>> valuefrequency = null;      //å¯¹é�¢å‡ºä»·ä¸­çš„valueçš„é¢‘çŽ‡
    private HashMap<Bid, Double> historyBids = null;                           //å¯¹é�¢å‡ºä»·åŽ†å�²----å¸¦ç�€å¯¹æˆ‘ä»¬æ�¥è¯´çš„æ”¶ç›Š

    //æ–¹æ³•ç”¨å�‚æ•°
    private Value issueValue = null;                                            //æŸ�ä¸ªissueçš„æŸ�ä¸ªvalue,æœ€å¥½æ”¾è¿›æ–¹æ³•é‡Œï¼�ï¼�ï¼�ï¼�ï¼�ï¼�
    private Double anumber = 0.0;                                                    //è®°å½•valueå‡ºçŽ°æ¬¡æ•°ï¼Œæœ€å¥½æ�¢ä¸ªå��å­—ï¼Œæ”¾è¿›æ–¹æ³•é‡Œï¼�ï¼�ï¼�ï¼�


    public negotatingInfo(NegotiationInfo info) {

        System.out.println("ä¿¡æ�¯åˆ�å§‹åŒ–å¼€å§‹");
        issuesList = info.getUserModel().getDomain().getIssues();               //é€šè¿‡ç”¨æ¨¡åž‹å¾—åˆ°issueåˆ—è¡¨
        valuefrequency = new HashMap<Issue, HashMap<Value, Double>>();         //æ–°å»ºä¸€ä¸ªvaluefrequency
        historyBids = new HashMap<Bid, Double>();                              //æ–°å»ºä¸€ä¸ªhistoryBids

        System.out.println("æˆ‘ä»¬å¾—åˆ°äº†è®ºç‚¹åˆ—è¡¨ï¼š" + issuesList);
        System.out.println("å¼€å§‹æž„å»ºå¯¹é�¢å‡ºä»·ä¸­çš„valueçš„é¢‘çŽ‡åˆ—è¡¨ï¼š" );

        for (Issue issue: issuesList){                                          //å¯¹äºŽvaluefrequencyä¸­çš„æ¯�ä¸€ä¸ªisuue keyï¼Œæ–°å»ºæ–°çš„å“ˆå¸Œmap
            //   System.out.println(issue);
            valuefrequency.put(issue, new HashMap<Value, Double>());
        }
        System.out.println("æž„å»ºå¯¹é�¢å‡ºä»·ä¸­çš„valueçš„é¢‘çŽ‡åˆ—è¡¨æˆ�åŠŸï¼Œå¾—åˆ°");
        System.out.println(issuesList);
        System.out.println("ä¿¡æ�¯åˆ�å§‹åŒ–ç»“æ�Ÿ");

    }


    public void storeGetOffer(Bid lastReceivedOffer, Double utilitySum) {

        System.out.println("è®°å½•å¯¹æ–¹ä¿¡æ�¯å‡ºä»·ï¼š" + lastReceivedOffer);
        for (Issue issue : issuesList) {
            issueValue = lastReceivedOffer.getValue(issue.getNumber());

            System.out.println("è®ºç‚¹ï¼š " + issue + "çš„å€¼ï¼š" + issueValue);

            if (!valuefrequency.get(issue).containsKey(issueValue)){
                System.out.println("å®ƒä¸�åœ¨è®°å½•ä¸­ï¼Œåˆ›å»ºæ–°è®°å½•");
                valuefrequency.get(issue).put(issueValue, 1.0);
                System.out.println("åˆ›å»ºæˆ�åŠŸ");
            } else {
                System.out.println("å®ƒä¸�åœ¨è®°å½•ä¸­ï¼Œå¼€å§‹ä¿®æ”¹");
                anumber =  valuefrequency.get(issue).get(issueValue);

                System.out.println("çŽ°åœ¨æœ‰é¢‘æ•°ï¼š" + valuefrequency);
                anumber = anumber + 1;
                valuefrequency.get(issue).put(issueValue, anumber);
                System.out.println("ä¿®æ”¹æˆ�åŠŸï¼�");
                System.out.println("çŽ°åœ¨æœ‰é¢‘æ•°ï¼š" + valuefrequency);
            }
        }
        System.out.println("valueé¢‘æ•°æ›´æ–°æˆ�åŠŸï¼Œå¼€å§‹è®°å½•å‡ºä»·æœ¬èº«");
        historyBids.put(lastReceivedOffer, utilitySum);
        System.out.println("è®°å½•æˆ�åŠŸï¼�");
        System.out.println(lastReceivedOffer);
        System.out.println(historyBids.get(lastReceivedOffer));
        System.out.println("æŽ¥å�—ä¿¡æ�¯è®°å½•ç»“æ�Ÿï¼�");
    }
}



class bidSearch {

    private List<Issue> issuesList;
    private  List<Bid> bids = null;

    private HashMap<Issue, HashMap<Value, Double>> frountPart = null;
    private HashMap<Issue, HashMap<Value, Double>> latterPart = null;
    private HashMap<Issue, HashMap<Value, Double>> biases = null;
    private HashMap<Value, Double> latterValueList = null;
    private HashMap<Value, Double> frountValueList = null;

    //å��å·®å€¼issueå†…æŽ’åº�ç”¨å�‚æ•°å£°æ˜Žï¼š
    private HashMap<Issue, LinkedHashMap<Value, Double>> BiasMap = null;
    //public LinkedHashMap<Value, Double> MapOfBias;

    //å��å·®å€¼ä¿®æ­£ç”¨
    private HashMap<Value, Double> issuesBiasesList = null;


    double minBiases = 0.0;


    public bidSearch(NegotiationInfo info) {

        System.out.println("åˆ�å§‹åŒ–ä»£ç�†ç­–ç•¥ç ”ç©¶");

        issuesList = info.getUserModel().getDomain().getIssues();
        bids = info.getUserModel().getBidRanking().getBidOrder();

        frountPart = new HashMap<Issue, HashMap<Value, Double>>();
        latterPart = new HashMap<Issue, HashMap<Value, Double>>();
        biases = new HashMap<Issue, HashMap<Value, Double>>();
        latterValueList = new HashMap<Value, Double>();
        frountValueList = new HashMap<Value, Double>();


        //å��å·®å€¼issueå†…æŽ’åº�ç”¨å�‚æ•°åˆ�å§‹ï¼š
        BiasMap= new HashMap<Issue, LinkedHashMap<Value, Double>>();
        //MapOfBias = new LinkedHashMap<Value, Double>();

        //å��å·®å€¼ä¿®æ­£ç”¨
        issuesBiasesList = new HashMap<Value, Double>();


        for (Issue issue : issuesList) {
            frountPart.put(issue,new HashMap<Value, Double>() );
            latterPart.put(issue,new HashMap<Value, Double>() );
        }



        System.out.println("å¾—åˆ°è®ºç‚¹åˆ—è¡¨ï¼š" + issuesList);
        System.out.println("å¾—åˆ°å‡ºä»·åˆ—è¡¨ï¼š " + bids);
        System.out.println("åˆ�å§‹åŒ–ä»£ç�†ç­–ç•¥ç ”ç©¶ç»“æ�Ÿ");

        System.out.println("ä»£ç�†å‡ºä»·æ•°é‡�ï¼š");
        int halfOfLength = (int) bids.size()/2;
        System.out.println(bids.size());
        System.out.println("ä»£ç�†å‡ºä»·æ•°é‡�/2");
        System.out.println(halfOfLength);
        System.out.println("ä»£ç�†å‡ºä»·æ•°é‡�å±•ç¤ºç»“æ�Ÿ");

        System.out.println("ä»£ç�†å‡ºä»·åˆ†ä¸¤æ®µ");
        System.out.println("ä»£ç�†å‡ºä»·å‰�æ®µ");

        for (int i = 0; i <= halfOfLength; i++) {

            System.out.println("è¿™æ˜¯ç¬¬ï¼š" + i + " ä¸ªå‡ºä»·");

            Bid tmpBid = bids.get(i);

            System.out.println("è¿™ä¸ªå‡ºä»·æ˜¯ï¼š" + tmpBid);

            System.out.println("å¯¹äºŽè¿™ä¸ªå‡ºä»·çš„æ¯�ä¸ªé€‰é¡¹ï¼š");
            //double putValue = i;


            for (Issue issue : issuesList) {
                Value avalue = tmpBid.getValue(issue.getNumber());

                System.out.println("è¿™ä¸ªå‡ºä»·çš„ " + issue + " çš„å€¼æ˜¯" + avalue);

                if (!frountPart.get(issue).containsKey(avalue)) {


                    frountPart.get(issue).put(avalue, 1.0);
                    System.out.println("å‰�æ®µè®°å½•ä¸­ " + issue + " çš„ " + avalue + " æ²¡åœ¨è®°å½•ä¸­ï¼Œæˆ‘ä»¬éœ€è¦�åœ¨è®°å½•ä¸­äº§ç”Ÿæ–°çš„key");

                    if (!latterPart.get(issue).containsKey(avalue)) {
                        latterPart.get(issue).put(avalue, 0.0);

                        System.out.println("å�Žæ®µè®°å½•ä¸­ " + issue + " çš„ " + avalue + " æ²¡åœ¨è®°å½•ä¸­ï¼Œæˆ‘ä»¬éœ€è¦�åœ¨è®°å½•ä¸­äº§ç”Ÿæ–°çš„key");

                    }
                } else {

                    System.out.println("å‰�æ®µè®°å½•ä¸­ " + issue + " çš„ " + avalue + " å·²åœ¨è®°å½•ä¸­ï¼Œæˆ‘ä»¬+1");
                    Double tmpNum = frountPart.get(issue).get(avalue);
                    tmpNum = tmpNum + 1.0;
                    frountPart.get(issue).replace(avalue, tmpNum);
                }

            }
        }

        System.out.println("ä»£ç�†å‡ºä»·å�Žæ®µ");
        for (int n = bids.size() - 1; n > halfOfLength ; n--){

            System.out.println("è¿™æ˜¯ç¬¬ï¼š" + n + " ä¸ªå‡ºä»·");

            Bid tmpBid =  bids.get(n);
            List< Issue > bidIssuesList = tmpBid.getIssues();

            System.out.println("å¯¹äºŽè¿™ä¸ªå‡ºä»·çš„æ¯�ä¸ªé€‰é¡¹ï¼š");
            //double putValue = n;
            for (Issue issue : bidIssuesList) {
                Value avalue = tmpBid.getValue(issue.getNumber());

                System.out.println("è¿™ä¸ªå‡ºä»·çš„ " + issue + " çš„å€¼æ˜¯" + avalue);

                if (!latterPart.get(issue).containsKey(avalue)){
                    latterPart.get(issue).put(avalue, 1.0);

                    System.out.println("å�Žæ®µè®°å½•ä¸­ " + issue + " çš„ " + avalue + " æ²¡åœ¨è®°å½•ä¸­ï¼Œæˆ‘ä»¬éœ€è¦�åœ¨è®°å½•ä¸­äº§ç”Ÿæ–°çš„key");

                    if (!frountPart.get(issue).containsKey(avalue)){
                        frountPart.get(issue).put(avalue, 0.0);

                        System.out.println("å‰�æ®µè®°å½•ä¸­ " + issue + " çš„ " + avalue + " æ²¡åœ¨è®°å½•ä¸­ï¼Œæˆ‘ä»¬éœ€è¦�åœ¨è®°å½•ä¸­äº§ç”Ÿæ–°çš„key");
                    }
                }else{

                    System.out.println("å�Žæ®µè®°å½•ä¸­ " + issue + " çš„ " + avalue + " å·²åœ¨è®°å½•ä¸­ï¼Œæˆ‘ä»¬+1");

                    Double tmpNum = latterPart.get(issue).get(avalue);
                    tmpNum = tmpNum + 1;
                    latterPart.get(issue).put(avalue, tmpNum);
                }
            }

        }
        System.out.println("ä»£ç�†å‡ºä»·åˆ†æ®µè®°å½•æˆ�åŠŸï¼Œä¸‹é�¢å¼€å§‹è®°å½•å‰�å�Žæ®µå��å·®å€¼");

        System.out.println("å¯¹äºŽæ¯�ä¸ªè®ºç‚¹");
        for (Issue issue : issuesList) {
            System.out.println("è®ºç‚¹ï¼š" + issue);
            latterValueList = latterPart.get(issue);
            biases.put(issue, new HashMap<Value, Double>());


            for (Value kay: latterValueList.keySet()){
                System.out.println("å¯¹äºŽè®ºç‚¹ " + issue + " çš„value :" + kay);
                Double latterValue = latterValueList.get(kay);
                Double frountValue = frountPart.get(issue).get(kay);
                System.out.println("å®ƒå�Žæ®µé¢‘çŽ‡æ˜¯ï¼š " + latterValue);
                System.out.println("å®ƒå‰�æ®µé¢‘çŽ‡æ˜¯ï¼š " + frountValue);
                Double biaeses = latterValue - frountValue;
                biases.get(issue).put(kay, biaeses);
                System.out.println("å®ƒçš„å��å·®å€¼æ˜¯ï¼š " +  biases.get(issue).get(kay));
                if (biaeses < minBiases){
                    minBiases = biaeses;
                }
            }
        }


        System.out.println("å��å·®å€¼è®¡ç®—æˆ�åŠŸ");
        System.out.println("å��å·®å€¼åˆ—è¡¨æ˜¯ï¼š" + biases);

        System.out.println("å��å·®å€¼ä¿®æ­£");
        if (minBiases < 0){
            minBiases = 0 - minBiases + 1;
            for (Issue issue : issuesList) {
                issuesBiasesList = biases.get(issue);
                for (Value kay: issuesBiasesList.keySet()){
                    Double oldBiases = issuesBiasesList.get(kay);
                    Double newBiases = oldBiases + minBiases;
                    biases.get(issue).replace(kay, newBiases);
                }

            }
        }
        System.out.println("ä¿®æ­£å�Žçš„å��å·®å€¼åˆ—è¡¨æ˜¯ï¼š" + biases);
//
//        System.out.println("å‡ºä»·æ�œç´¢æš‚æ—¶ç»“æ�Ÿ");
//
        //å‡ºä»·åˆ—è¡¨å¯¹åº”çš„å��å·®å€¼å±•ç¤º
//        System.out.println("å·±æ–¹å‡ºä»·å��å·®æŽ’åº�ï¼Œåº”è¯¥ä»Žå°�åˆ°å¤§");
//        for (Bid bid: bids){
//            double biasesSum = 0.0;
//            for (Issue issue: issuesList){
//                //System.out.println("ä¸€æ¬¡å°�è¯•");
//                Value issueValue = bid.getValue(issue.getNumber());
//                // System.out.println("è¿™ä¸ªæŠ¥ä»·åœ¨ " + issue +  "ä¸Šçš„å€¼ä¸ºï¼š" + issueValue );
//                biasesSum = biasesSum + biases.get(issue).get(issueValue);
//            }
//            System.out.println(biasesSum);
//        }



        //å�Žé�¢æ˜¯å¯¹å��å·®å€¼æŽ’åº�ç”¨
        for (Issue issue : issuesList) {

            BiasMap.put(issue, new LinkedHashMap<Value, Double>());
            List<Map.Entry<Value, Double>> listOfBias = new ArrayList<Map.Entry<Value, Double>>(biases.get(issue).entrySet());

            Collections.sort(listOfBias,new Comparator<Map.Entry<Value, Double>>(){
                @Override
                public int compare (Map.Entry < Value, Double > o1,
                                    Map.Entry <Value, Double> o2){
                    if (o1.getValue() > o2.getValue()) {
                        return -1;
                    } else  {
                        return 1;
                    }}
            });

            for(int i = 0; i < listOfBias.size(); i++){
                //MapOfBias.put(listOfBias.get(i).getKey(),listOfBias.get(i).getValue());//??????????æœ‰ä»€ä¹ˆç”¨ï¼Ÿï¼Ÿï¼Ÿï¼Ÿ
                BiasMap.get(issue).put(listOfBias.get(i).getKey(),listOfBias.get(i).getValue());
            }
            // BiasMap.put(issue, new LinkedHashMap<Value, Double>())
        }
        System.out.println("12345");
        System.out.println(BiasMap);
        System.out.println("12345");
    }

    public HashMap<Issue, HashMap<Value, Double>> getBiases() {
        System.out.println("å��å·®å€¼è¢«è°ƒç”¨");
        return  biases;
    }

    public  HashMap<Issue, LinkedHashMap<Value, Double>> getBiasMap(){
        System.out.println("æŽ’åº�å��å·®å€¼è¢«è°ƒç”¨");
        return BiasMap;
    }

}