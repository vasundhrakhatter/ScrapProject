package nexthoughts

import com.nexthoughts.VO.BankAccountVO
import com.nexthoughts.VO.BankTransactionVO
import com.nexthoughts.VO.Enums
import geb.Browser

import java.text.SimpleDateFormat
import java.util.regex.Matcher
import java.util.regex.Pattern;

class YesBankController {

    def index() {
        Browser.drive() {
            try {
                config.reportsDir = new File("target/reports")
                go("https://www.yesbank.in/retail-disclaimer")
                report("${title}")

                $('a.btn', href: 'javascript:;').click()
                $('input', class: 'input1', name: 'fldLoginUserId').value('some_username')
                $('input', class: 'input1', name: 'fldPassword').value('some_pwd')
                $('img', alt: 'Login').click()
                Thread.sleep(5000)
                println("Title =========>${title}")
                report("${title}")

                BankAccountVO bankAccountVO = new BankAccountVO()
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy")

                withFrame($('frame', name: 'main')) {
                    report("Welcome")
                    $('table.TableBorder').find('tr.AlterRow1').find('td.DataLeftAligned').eq(1).find('a').click()

                    def tableContents = $('table.TableBorder')[1]
                    tableContents.find('tr').eachWithIndex { value, i ->
                        if (i == 0) {
                            String accountinfo = value.find('td.TableHeading').text()
                            String accountNumberRegex = "([0-9]{15})"
                            Pattern accountNumberpattern = Pattern.compile(accountNumberRegex)
                            Matcher accountNumbermatcher = accountNumberpattern.matcher(accountinfo)
                            if (accountNumbermatcher.find()) {
                                bankAccountVO.accountNo = accountNumbermatcher.group() as Long
                                println("accountNumber===========>" + bankAccountVO.accountNo)
                            }
                        } else if (i == 1) {
                            String accountHolderInfo = value.find('td.DataLeftAligned').text()
                            bankAccountVO.accountHolders << accountHolderInfo
                            println "name=================>" + bankAccountVO.accountHolders
                        } else if (value.find('td.DataLeftAlignedBold').text().equals("Address")) {
                            if ((i - 1) != 1) {
                                for (j in [2..i - 1]) {
                                    String accountHoldersinfo = tableContents.find('tr').eq(j).find('td.DataLeftAligned').text()
                                    bankAccountVO.accountHolders << accountHoldersinfo
                                    println "multipleAccountHolders=========>" + bankAccountVO.accountHolders
                                }
                            }
                        } else if (value.find('td.DataLeftAlignedBold').text().equals("Email")) {
                            String emailInfo = value.find('td.DataLeftAligned').text()
                            bankAccountVO.email = emailInfo
                            println("email=====================>" + emailInfo)
                        }
                    }
                    $('table').find('td.navbold')[0].find('a').find('img', alt: 'account summary').click()
                    $('table.TableBorder').find('tr.AlterRow1').find('td.DataLeftAligned').eq(0).find('a').click()

                    String openingBalance = $('table.TableBorder').find('td.DataLeftAligned')[0].text()
                    String desc1 = $('table.TableBorder').find('td.DataLeftAligned')[0].find('b').text()
                    String finalopening = openingBalance - desc1.trim()
                    Double openingAmount = Double.parseDouble(finalopening)
                    bankAccountVO.openingBalance = openingAmount
                    println "opening Balance=============>" + bankAccountVO.openingBalance

                    String closingBalance = $('table.TableBorder').find('td.DataLeftAligned')[1].text()
                    String desc2 = $('table.TableBorder').find('td.DataLeftAligned')[1].find('b').text()
                    String finalclosing = closingBalance - desc2.trim()
                    Double closingAmount = Double.parseDouble(finalclosing)
                    bankAccountVO.closingBalance = closingAmount
                    println "closing Balance=============>" + bankAccountVO.closingBalance

                    Boolean loop = true
                    while (loop) {
                        $('table.TableBorder')[1].find('tr').eachWithIndex { value, i ->
                            if (i > 0) {
                                BankTransactionVO bankTransactionVO = new BankTransactionVO()

                                String date = value.find('td.DataLeftAligned')[0].text()
                                bankTransactionVO.dateTime = dateFormat.parse(date)
                                println "Date============>" + bankTransactionVO.dateTime

                                bankTransactionVO.particulars = value.find('td.DataLeftAligned')[3].text()
                                println "particulars===============>" + bankTransactionVO.particulars

                                String debit = value.find('td.DataRightAligned')[0].text()
                                String credit = value.find('td.DataRightAligned')[1].text()
                                bankTransactionVO.amount = debit == 0.00 ? Double.parseDouble(credit.trim().replaceAll(",", "")) : Double.parseDouble(debit.trim().replaceAll(",", ""))
                                println "amount==============>" + bankTransactionVO.amount

                                bankTransactionVO.type = !debit ? Enums.bankTransactionType.CREDIT : Enums.bankTransactionType.DEBIT
                                println "type=============>" + bankTransactionVO.type

                                String balance = value.find('td.DataRightAligned')[2].text()
                                bankTransactionVO.balanceAfterTransaction = Double.parseDouble(balance.trim().replaceAll(",", ""))
                                println "balance==============>" + bankTransactionVO.balanceAfterTransaction

                                bankAccountVO.bankTransactionList << bankTransactionVO
                            }
                        }
                        if ($('table').find('td.DataRightAlignedWhite')[1].find('a').find('img', src: 'images/eng/blue/next_icon.gif')) {
                            $('table').find('td.DataRightAlignedWhite')[1].find('a').find('img', src: 'images/eng/blue/next_icon.gif').click()
                        } else {
                            loop = false
                        }

                        println bankAccountVO.bankTransactionList
                    }
                }
            }
            finally {
                String frameName = $('frameset')[0].find('frame', title: 'top menu frame')[0].attr('name')
                withFrame($('frame', name: frameName)) {
                    $('td.loginDetails').find('td.logintext').find('a', text: 'Logout').click()
                }
            }
        }
    }
}
