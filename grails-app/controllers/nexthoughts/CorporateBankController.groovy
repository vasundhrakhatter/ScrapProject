package nexthoughts

import geb.Browser;
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.text.SimpleDateFormat
import com.nexthoughts.VO.*

class CorporateBankController {

    def index() {
        Browser.drive() {

            try {
                BankAccountVO bankAccountVO = new BankAccountVO()
                config.reportsDir = new File("target/corporateBankReports")
                go("https://www.corpretail.com/RetailBank/jsp/common/PreLogin.jsp")
                report("title=========>${title}")
                Thread.currentThread().sleep(3000)
                println "${title}"

                $('#userId1').value('some_name')
                $('input', id: 'termscondition').click()
                $('input.continue').click()
                report("title=========>${title}")
                println "${title}"

                $('input', name: 'password1').value('some_pwd')
                $('input', id: 'imagecheck').click()
                $('input.login')[0].click()
                report("title=========>${title}")
                println "${title}"

                String name = $('td span.topright strong')[0].text()
                bankAccountVO.accountHolder = name
                println "Name============> ${bankAccountVO.accountHolder}"

                $('a#detailLinkHide0.displayLink').click()
                report("title=========>${title}")
                println "${title}"

                $('div#hid_buttons').find('a', href: 'javascript:SubmitForm();').find('span.displayButtonText').click()
                report("title=========>${title}")
                println "${title}"

                List<String> summary = $('table.bordertable').find('table.nobordertable').find('td span.displayLabel')*.text()
                println "Summary======>" + summary

                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy")
                String dateRegex = "^[0-3]?[0-9]/[0-3]?[0-9]/(?:[0-9]{2})?[0-9]{2}"
                Pattern datePattern = Pattern.compile(dateRegex)
                Matcher dateMatcher

                summary.eachWithIndex { value, i ->
                    if (value.find('BRANCH[\\s]*:')) {
                        String branch = value.substring(value.indexOf(":"), value.indexOf("/")).trim().replaceAll(":", "").trim()
                        bankAccountVO.branch = branch
                        println "Branch===========> ${bankAccountVO.branch}"

                        String ifscCodeRegex = "(CORP[0-9]{7})"
                        Pattern pattern = Pattern.compile(ifscCodeRegex)
                        Matcher matcher1 = pattern.matcher(value)
                        if (matcher1.find()) {
                            String ifscCode = matcher1.group() as String
                            bankAccountVO.ifscCode = ifscCode
                            println "IFSC code===========>" + bankAccountVO.ifscCode
                        }

                        String accountNumberRegex = "([0-9]{15})"
                        Pattern accountNumberpattern = Pattern.compile(accountNumberRegex)
                        Matcher accountNumbermatcher = accountNumberpattern.matcher(value)
                        if (accountNumbermatcher.find()) {
                            Long accountNo = accountNumbermatcher.group() as Long
                            bankAccountVO.accountNo = accountNo
                            println "Account Number===========>" + bankAccountVO.accountNo
                        }
                    } else {
                        dateMatcher = datePattern.matcher(value)
                        if (dateMatcher.find()) {
                            BankTransactionVO bankTransactionVO = new BankTransactionVO()
                            bankTransactionVO.dateTime = dateFormat.parse(summary[i])
                            if (summary[i + 1].equals("Opening Balance")) {
                                Double balance = Double.parseDouble(summary[i + 6].trim().replaceAll(",", ""))
                                bankAccountVO.openingBalance = balance
                                println "openingBalance======>" + bankAccountVO.openingBalance
                            } else if (summary[i + 1].equals("Closing Balance")) {
                                Double balance = Double.parseDouble(summary[i + 6].trim().replaceAll(",", ""))
                                bankAccountVO.closingBalance = balance
                                println "closingBalance======>" + bankAccountVO.closingBalance
                            } else {
                                if (!summary[i + 4].isEmpty()) {
                                    Double withdraw = Double.parseDouble(summary[i + 4].trim().replaceAll(",", ""))
                                    Double balance = Double.parseDouble(summary[i + 6].trim().replaceAll(",", ""))
                                    bankTransactionVO.type = Enums.bankTransactionType.DEBIT
                                    println "type=====>" + bankTransactionVO.type
                                    bankTransactionVO.amount = withdraw
                                    println "amount======>" + bankTransactionVO.amount
                                    bankTransactionVO.balanceAfterTransaction = balance
                                    println "balance======>" + bankTransactionVO.balanceAfterTransaction
                                    bankTransactionVO.particulars = summary[i + 1]
                                    println "particular======>" + bankTransactionVO.particulars
                                } else if (!summary[i + 5].isEmpty()) {
                                    Double deposit = Double.parseDouble(summary[i + 5].trim().replaceAll(",", ""))
                                    Double balance = Double.parseDouble(summary[i + 6].trim().replaceAll(",", ""))
                                    bankTransactionVO.type = Enums.bankTransactionType.CREDIT
                                    println "type=====>" + bankTransactionVO.type
                                    bankTransactionVO.amount = deposit
                                    println "amount======>" + bankTransactionVO.amount
                                    bankTransactionVO.balanceAfterTransaction = balance
                                    println "balance======>" + bankTransactionVO.balanceAfterTransaction
                                    bankTransactionVO.particulars = summary[i + 1]
                                    println "particular======>" + bankTransactionVO.particulars
                                }
                            }
                            bankAccountVO.bankTransactionList << bankTransactionVO
                        }
                    }
                }

            }

            finally {
                $('a.topright1', href: 'javascript:logout();').click()
            }

        }
    }
}

