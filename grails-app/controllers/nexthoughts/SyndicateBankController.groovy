package nexthoughts

import geb.Browser;
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.text.SimpleDateFormat
import com.nexthoughts.VO.*


class SyndicateBankController {

    def index() {

    }

    def test() {

        Browser.drive() {
            try {

                config.reportsDir = new File("target/syndicateBankReposrts")
                go('https://www.syndonline.in/B001/ENULogin.jsp')
                BankAccountVO bankAccountVO = new BankAccountVO()
                $('div.LoginTableUp').find('table td').find('input', name: 'fldLoginUserId').value('some_name')
                $('div.LoginTableUp').find('table td').find('input.highLight', name: 'fldLoginUserId').value('some_name')
                $('div.LoginTableUp').find('table td').find('input', name: 'fldPassword').value('some_pwd')
                $('div.LoginTableUp').find('table td').find('input.highLight', name: 'fldPassword').value('some_pwd')
                withNewWindow({
                    $('div.LoginTableUp').find('table td').find('input.btnsignin', value: 'Login').click()
                }, close: false) {
                    println "afterlogin=====> ${title}"
                    Thread.sleep(40000)
                    withFrame($('frame', name: 'frame_menu')) {
                        $('a#RRAAClink').click()
                    }
                    withFrame($('frame', name: 'frame_txn')) {
                        $('select', name: 'fldacctno').value("91032210002489  ~C~9038~22245328").click()
                        $('select#fldsearch.objselect').value("8").click()
                        $('input.Buttons', value: 'Submit').click()

                        String accountHolderInfo = $('span#box')[3].find('tr.AlterRow2').find('label.labeltext')[0].text()
                        bankAccountVO.accountHolders << accountHolderInfo
                        println "account holder=============>" + bankAccountVO.accountHolders

                        String branchInfo = $('span#box')[3].find('tr.AlterRow2').find('label.labeltext')[1].text()
                        bankAccountVO.branch = branchInfo
                        println "branch==============>" + bankAccountVO.branch

                        String accountNumberInfo = $('span#box')[5].find('tr.AlterRow2').find('label.labeltext')[0].text()
                        bankAccountVO.accountNo = Double.parseDouble(accountNumberInfo.trim())
                        println "accountNumber=============>" + bankAccountVO.accountNo

                        String openingBalanceInfo = $('span#box')[5].find('tr.AlterRow2').find('td.ccyalign')[0].text()
                        bankAccountVO.openingBalance = Double.parseDouble(openingBalanceInfo.trim().replaceAll(",", ""))
                        println "openingBalance==============>" + bankAccountVO.openingBalance

                        String closingBalanceInfo = $('span#box')[5].find('tr.AlterRow2').find('td.ccyalign')[1].text()
                        bankAccountVO.closingBalance = Double.parseDouble(closingBalanceInfo.trim().replaceAll(",", ""))
                        println "closingBalance==============>" + bankAccountVO.closingBalance

                        int index = 1
                        while (index > 0) {
                            def transactionInfoTable = $('span#Box')[6].find('table.graphtable')
                            transactionInfoTable.find('tr').eachWithIndex { value, i ->
                                if (i > 0) {
                                    BankTransactionVO bankTransactionVO = new BankTransactionVO()
                                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy")

                                    String dateInfo = value.find('td')[0].text()
                                    bankTransactionVO.dateTime = dateFormat.parse(dateInfo)
                                    println "date================>" + bankTransactionVO.dateTime

                                    bankTransactionVO.particulars = value.find('td')[3].text()
                                    println "particulars====================>" + bankTransactionVO.particulars

                                    String debit = value.find('td.ccyalign')[0].text()
                                    String credit = value.find('td.ccyalign')[1].text()
                                    String balance = value.find('td.ccyalign')[2].text()
                                    if (debit) {
                                        bankTransactionVO.amount = Double.parseDouble(debit.trim().replaceAll(",", ""))
                                        bankTransactionVO.type = Enums.bankTransactionType.DEBIT
                                    } else if (credit) {
                                        bankTransactionVO.amount = Double.parseDouble(credit.trim().replaceAll(",", ""))
                                        bankTransactionVO.type = Enums.bankTransactionType.CREDIT
                                    }
                                    bankTransactionVO.balanceAfterTransaction = Double.parseDouble(balance.trim().replaceAll(",", ""))
                                    println "amount===============>" + bankTransactionVO.amount
                                    println "type==================>" + bankTransactionVO.type
                                    println "balance==================>" + bankTransactionVO.balanceAfterTransaction

                                    bankAccountVO.bankTransactionList << bankTransactionVO
                                }

                            }
                            String indextext = index + 1 as String
                            if ($('table.standardtable').find('td.labeltext.col1').find('a', text: indextext)) {
                                index++
                                $('table.standardtable').find('td.labeltext.col1').find('a', text: indextext).click()

                            } else {
                                index = 0
                            }
                        }
                    }

                }
            }
            finally {
                withFrame($('frame', title: 'top menu frame', name: 'frame_top')) {
                    $('div.topnavbar').find('a', text: 'Logout').click()
                }
            }
        }
    }
}


