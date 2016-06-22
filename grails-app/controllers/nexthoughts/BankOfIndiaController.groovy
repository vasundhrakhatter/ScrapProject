package nexthoughts
import com.nexthoughts.VO.BankAccountVO
import com.nexthoughts.VO.BankTransactionVO
import com.nexthoughts.VO.Enums
import geb.Browser

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern

class BankOfIndiaController {

    def index() {

        Browser.drive {
            try {
                go("http://www.bankofindia.co.in/english/startRedtpg.html")
                $('input', id: "CorporateSignonCorpId").value("some_username")
                $('input', name: "CorporateSignonPassword").value("some_password")
                $('input#button1').click()

                //<--------Welcome!-------->
               $('table.table')[1].find('tr.row2',valign:'middle')[0].find('td a b')[0].click()

                //<---------SavingsAccountSummary------------>
                $('table',cellspacing:'1')[0].find('tr.row2')[0].find('a').click()

                //<---------QuickView---------------->
                $('select',name:"Options.SelectList").value("Option.Accounts.AccountDetails").click()
                $('input', value:"Go").click()

                //<----------OperativeAccount--------->
                BankAccountVO bankAccountVO=new BankAccountVO()

                bankAccountVO.accountHolders << $('tr.row3').find('td')[1].find('b').text()
                println "accountHolder========================>"+bankAccountVO.accountHolders

                bankAccountVO.accountNo= ($('tr.row2').find('td')[1].find('b').text()) as Long
                println "accountNumber===================>"+bankAccountVO.accountNo

                String openingDate= ($('tr.row2')[4].find('td')[1].text()).trim()
                DateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH)

                String closingBalanceInfo=($('tr.row2')[5].find('td')[1].text()).trim().replaceAll(",","")
                bankAccountVO.closingBalance=Double.parseDouble(closingBalanceInfo)
                println "closingBalance=====================>"+bankAccountVO.closingBalance

                $('select',name:"Options.SelectList").value("Option.Accounts.QuerySelection").click()
                $('input', value:"Go").click()

                //<--------AccountStatement---------->
                js.exec("document.getElementById('txnSrcFromDate').removeAttribute('readOnly')")
                $('input#txnSrcFromDate').value(openingDate)
                bankAccountVO.fromDate=dateFormat.parse(openingDate)
                println "From=================>"+bankAccountVO.fromDate
                js.exec("document.getElementById('txnSrcToDate').removeAttribute('readOnly')")
                $('input#txnSrcToDate').value(new Date().format("MMMM dd, yyyy"))
                bankAccountVO.toDate=dateFormat.parse($('input#txnSrcToDate').value())
                println "To===================>"+bankAccountVO.toDate
                $('input',name:'Action.Accounts.QuerySelection.QueryStatement').click()

                //<---------Full Statement------------>
                String dateRegex = "((January|February|March|April|May|June|July|August|September|October|November|December)\\s*[0-9]{2},\\s*[0-9]{4})"
                Pattern datepattern = Pattern.compile(dateRegex)
                bankAccountVO.openingBalance=-1
                def transactionInfo=$('table.table')[1]
                transactionInfo.find('tr').eachWithIndex{ value, i ->
                    if(i>0){
                        BankTransactionVO bankTransactionVO=new BankTransactionVO()
                        Matcher dateMatcher = datepattern.matcher(value.find('td')[1].text())
                        if(dateMatcher.find()){
                            String dateTime = dateMatcher.group() as String
                            bankTransactionVO.dateTime = dateFormat.parse(dateTime)
                            println "date=============>" + bankTransactionVO.dateTime
                        }
                        bankTransactionVO.particulars = value.find('td')[2].text()
                        println "particulars================>" + bankTransactionVO.particulars

                        bankTransactionVO.reference = value.find('td')[3].text()
                        println "reference Cheque==============>" + bankTransactionVO.reference

                        String debit = value.find('td')[5].text().trim().replaceAll(",", "")
                        String credit = value.find('td')[6].text().trim().replaceAll(",", "")
                        String balance = value.find('td')[7].text().trim().replaceAll(",", "")
                        bankTransactionVO.amount = debit.isEmpty()? Double.parseDouble(credit) : Double.parseDouble(debit)
                        println "amount=============>" + bankTransactionVO.amount
                        if(bankAccountVO.openingBalance==-1){
                            bankAccountVO.openingBalance=bankTransactionVO.amount
                            println "openingg balance========>"+bankAccountVO.openingBalance
                        }
                        bankTransactionVO.type = debit.isEmpty() ? Enums.bankTransactionType.CREDIT : Enums.bankTransactionType.DEBIT
                        println "type=================>" + bankTransactionVO.type
                        bankTransactionVO.balanceAfterTransaction = Double.parseDouble(balance)
                        println "balance==============>" + bankTransactionVO.balanceAfterTransaction

                        bankAccountVO.bankTransactionList << bankTransactionVO
                    }
                }
            }
            finally {
              $('input',name:"Action.CorpUser.SignoffMain").click()

            }

        }
    }
}
