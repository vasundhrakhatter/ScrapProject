package nexthoughts
import geb.Browser;
import com.nexthoughts.VO.*

import java.text.SimpleDateFormat

class BankOfBarodaController {

    def index() {

        Browser.drive() {
            try {
                BankAccountVO bankAccountVO = new BankAccountVO()
                //    <--------Login page---------->
                go("file:///C:/Users/a/Downloads/Bank_of_Baroda_NetBanking-2016-06-16/Bank%20of%20Baroda%20NetBanking/BarodaConnect%20-%20A%20Hi-Tech%20Convenience%20eBanking%20Product%20suite%20of%20Bank%20of%20Baroda%20-%20India's%20International%20Bank.html")
                $('input#txtLoginID').value("Some_name")
                $('input#btnSubmit').click()

                //<-------PasswordPage---------->
                go("file:///C:/Users/a/Downloads/Bank_of_Baroda_NetBanking-2016-06-16/Bank%20of%20Baroda%20NetBanking/BarodaConnect%20-%20passoword%20page.html")
                $('input#txtPassword').value("Some_password")
                $('input#button1').click()

                // <---------Home-Landing Page------->
                go("file:///C:/Users/a/Downloads/Bank_of_Baroda_NetBanking-2016-06-16/Bank%20of%20Baroda%20NetBanking/Bank%20of%20Baroda%20landing%20after%20login.html")
                $('a#accountNav').click()
                go("file:///C:/Users/a/Downloads/Bank_of_Baroda_NetBanking-2016-06-16/Bank%20of%20Baroda%20NetBanking/Bank%20of%20Baroda%20accounts%20summary%20page.html")
                $('a', title: 'Operative Accounts ').click()

                bankAccountVO.branch = $('div.mainPanelInner').find('div.pdt5').find('tr')[1].find('td')[4].text()
                println "branch=========>" + bankAccountVO.branch

                $('div#oprDeActive').find('input#oprDeActive')[2].click()
                go("file:///C:/Users/a/Downloads/Bank_of_Baroda_NetBanking-2016-06-16/Bank%20of%20Baroda%20NetBanking/Bank%20of%20Baroda%20-%20transactions.html")
                //<----------Transactions Information------->

                $('input', value: 'Show Details').click()
                String accountNumberInfo = $('div.detailBlkview').find('div.bold')[0].text()
                bankAccountVO.accountNo = Long.parseLong(accountNumberInfo)
                println("accountNumber===========>" + bankAccountVO.accountNo)

                bankAccountVO.accountHolders << $('div.detailBlkview').find('div.bold')[1].text()
                println("accountHolder=========>" + bankAccountVO.accountHolders)

                if ($('div.detailBlkview').find('div.ltFlt.lpd25')[1]) {
                    bankAccountVO.accountHolders << $('div.detailBlkview').find('div.bold')[2].text()
                    println("accountHolder=========>" + bankAccountVO.accountHolders)
                }

                def transactionInfo = $('div#viewStatement').find('div.pdt5').find('table')
                transactionInfo.find('tr').eachWithIndex { value, i ->
                    if (i > 1) {
                        BankTransactionVO bankTransactionVO = new BankTransactionVO()
                        SimpleDateFormat dateFormat = new SimpleDateFormat('dd/MM/yyyy')

                        String dateInfo = value.find('td.centerAlign')[1].text()
                        bankTransactionVO.dateTime = dateFormat.parse(dateInfo)
                        println "date=============>" + bankTransactionVO.dateTime

                        bankTransactionVO.particulars = value.find('td.leftAlign')[0].text()
                        println "particulars============>" + bankTransactionVO.particulars

                        String amount = value.find('td', align: 'RIGHT')[0].text()
                        if (amount) {
                            bankTransactionVO.amount = Double.parseDouble(amount.trim().replaceAll(",", ""))
                            println "amount=================>" + bankTransactionVO.amount
                        }

                        String balance = value.find('td', align: 'right')[0].text()
                        if (balance) {
                            bankTransactionVO.balanceAfterTransaction = Double.parseDouble(balance.trim().replaceAll(",", ""))
                            println "balance==============>" + bankTransactionVO.balanceAfterTransaction
                        }

                        String previousbalance = transactionInfo.find('tr')[i - 1].find('td', align: 'right')[0].text()
                        if (previousbalance) {
                            Double previousBalance = Double.parseDouble(previousbalance.trim().replaceAll(",", ""))
                            bankTransactionVO.type = previousBalance > bankTransactionVO.balanceAfterTransaction ? Enums.bankTransactionType.DEBIT : Enums.bankTransactionType.CREDIT
                            println "type================>" + bankTransactionVO.type
                        }


                        bankAccountVO.bankTransactionList << bankTransactionVO

                    }
                }
            }
            finally {
                $('a.log.txtOrg', text: 'Logout').click()
            }
        }
    }
}
