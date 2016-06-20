package nexthoughts
import geb.Browser;
import com.nexthoughts.VO.*

import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

class PunjabNationalBankController {

    def index() {
        Browser.drive() {
            try {
                BankAccountVO bankAccountVO = new BankAccountVO()
                SimpleDateFormat dateFormat=new SimpleDateFormat("dd/MM/yyyy")
                //<-----------LoginPage------------->
//                go("file:///C:/Users/a/Downloads/PNB_Net_Banking-2016-06-20/PNB%20Net%20Banking/PNB%20E-Banking%20Existing%20users%20login%20USERNAME.htm")
//                $('input',id:'AuthenticationFG.USER_PRINCIPAL').value("some_username")
//                $('input',id:'STU_VALIDATE_CREDENTIALS').click()

                //<-----------PasswordPage------------>
//                go("file:///C:/Users/a/Downloads/PNB_Net_Banking-2016-06-20/PNB%20Net%20Banking/PNB%20E-Banking%20Existing%20users%20login%20PASSWORD.htm")
//                $('input',id:"AuthenticationFG.ACCESS_CODE").value("some_pwd")
//                $('input',id:"VALIDATE_STU_CREDENTIALS1").click()

                //<----------HomeLandingPage------------->
//                 go("file:///C:/Users/a/Downloads/PNB_Net_Banking-2016-06-20/PNB%20Net%20Banking/PNB%20E-Banking%20Personal%20Profile%20LANDING.htm")
//                 $('a#My-ShortCuts_Account-Statement').click()

                //<----------TransactionDetails------------>
                go("file:///C:/Users/a/Downloads/PNB_Net_Banking-2016-06-20/PNB%20Net%20Banking/PNB%20E-Banking%20My%20Transactions%20STATEMENT%20PAGE.htm")
                String info = $('span.left.gradientbgtwolinetxt').text()
                bankAccountVO.accountHolders << info.substring(info.indexOf("-"), info.indexOf("D/O")).trim().replaceAll("-", "").trim()
                println "name:================>" + bankAccountVO.accountHolders

                String accountNumberRegex = "([0-9]{15})"
                Pattern accountNumberpattern = Pattern.compile(accountNumberRegex)
                Matcher accountNumbermatcher = accountNumberpattern.matcher(info)
                if (accountNumbermatcher.find()) {
                    Long accountNo = accountNumbermatcher.group() as Long
                    bankAccountVO.accountNo = accountNo
                    println "Account Number===========>" + bankAccountVO.accountNo
                }

                boolean index=true
//                while (index){
                $('table#txnHistoryList').find('tr').eachWithIndex { value, i ->
                    String indexValue = index as String
                    if (i > 1 && i < 12) {
                        BankTransactionVO bankTransactionVo = new BankTransactionVO()

                        String dateInfo = value.find("td", class: 'listgreyrowtxtleftline')[0].text()
                        bankTransactionVo.dateTime= dateFormat.parse(dateInfo)
                        println "date:====================>"+bankTransactionVo.dateTime

                        bankTransactionVo.particulars=value.find("td",class:"listgreyrowtxtleftline")[2].text()
                        println "particulars:============>"+bankTransactionVo.particulars

                        String debit=value.find("td",class:"listgreyrowtxtleftline")[3].text()
                        String credit=value.find("td",class:"listgreyrowtxtleftline")[4].text()

                        bankTransactionVo.amount = debit.isAllWhitespace()? Double.parseDouble(credit.trim().replaceAll(",", "")) : Double.parseDouble(debit.trim().replaceAll(",", ""))
                        bankTransactionVo.type = debit.isAllWhitespace() ? Enums.bankTransactionType.CREDIT : Enums.bankTransactionType.DEBIT
                        println "amount====================>" + bankTransactionVo.amount
                        println "type:==================>"+bankTransactionVo.type

                        String balance=value.find('td',class:'amountrightalign')[0].text()
                        bankTransactionVo.balanceAfterTransaction=Double.parseDouble(balance.trim().replaceAll(",",""))
                        println "balance====================>"+bankTransactionVo.balanceAfterTransaction

                        bankAccountVO.bankTransactionList << bankTransactionVo
                    }
//                    String next=$('input',id:"Action.OpTransactionListing.GOTO_NEXT__").attr('disabled')
//                    if(next.equals("disabled")){
//                        index=false
//                    }
//                    else {
//                        $('input',id:"Action.OpTransactionListing.GOTO_NEXT__").click()
//                    }
//                }
                }
            }
            finally {
//                $('a#HREF_Logout').click()
            }
        }
    }
}
