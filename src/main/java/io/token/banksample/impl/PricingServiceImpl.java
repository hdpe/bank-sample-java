package io.token.banksample.impl;

import static io.token.proto.common.token.TokenProtos.TransferTokenStatus.FAILURE_DESTINATION_ACCOUNT_NOT_FOUND;
import static io.token.proto.common.token.TokenProtos.TransferTokenStatus.FAILURE_INSUFFICIENT_FUNDS;
import static io.token.proto.common.token.TokenProtos.TransferTokenStatus.FAILURE_INVALID_CURRENCY;
import static io.token.proto.common.token.TokenProtos.TransferTokenStatus.FAILURE_SOURCE_ACCOUNT_NOT_FOUND;

import io.token.banksample.model.Accounting;
import io.token.banksample.model.Pricing;
import io.token.proto.common.account.AccountProtos.BankAccount;
import io.token.proto.common.pricing.PricingProtos.TransferQuote;
import io.token.proto.common.transferinstructions.TransferInstructionsProtos.PurposeOfPayment;
import io.token.proto.common.transferinstructions.TransferInstructionsProtos.TransferEndpoint;
import io.token.sdk.api.Balance;
import io.token.sdk.api.PrepareTransferException;
import io.token.sdk.api.service.PricingService;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Sample implementation of the {@link PricingService}. Returns fake data.
 *
 * TODO: Make counterparty quote optional.
 */
public class PricingServiceImpl implements PricingService {
    private final Accounting accounts;
    private final Pricing pricing;

    public PricingServiceImpl(Accounting accounts, Pricing pricing) {
        this.accounts = accounts;
        this.pricing = pricing;
    }

    /**
     * FX is performed on the remitter side. Therefore the quote is from the
     * source to the destination account currency.
     *
     * {@inheritDoc}
     */
    @Override
    public TransferQuote prepareDebit(
            String tokenRefId,
            BigDecimal amount,
            String currency,
            BankAccount source,
            TransferEndpoint destination,
            TransferQuote counterpartyQuote,
            PurposeOfPayment paymentPurpose,
            Optional<TransferQuote> debitQuote) {
        Optional<Balance> balance = accounts.lookupBalance(source);
        if (!balance.isPresent()) {
            throw new PrepareTransferException(
                    FAILURE_SOURCE_ACCOUNT_NOT_FOUND,
                    "Account not found: " + destination);
        }

        if (balance.get().getAvailable().compareTo(amount) < 0) {
            throw new PrepareTransferException(
                    FAILURE_INSUFFICIENT_FUNDS,
                    "Balance exceeded");
        }

        String targetCurrency = counterpartyQuote.getAccountCurrency().isEmpty()
                ? currency
                : counterpartyQuote.getAccountCurrency();
        return debitQuote
                .map(quote -> pricing.lookupQuote(quote.getId()))
                .orElseGet(() -> pricing.debitQuote(
                        balance.get().getCurrency(),
                        targetCurrency));
    }
    /**
     * Preparing the credit on the beneficiary side. We don't support
     * beneficiary side FX yet, so we return an empty quote after
     * checking that account is present and has the right currency.
     *
     * {@inheritDoc}
     */
    @Override
    public TransferQuote prepareCredit(
            String tokenRefId,
            BigDecimal amount,
            String currency,
            TransferEndpoint source,
            TransferEndpoint destination,
            PurposeOfPayment paymentPurpose,
            Optional<TransferQuote> creditQuote) {
        Balance balance = accounts
                .lookupBalance(destination.getAccount())
                .orElseThrow(() -> new PrepareTransferException(
                        FAILURE_DESTINATION_ACCOUNT_NOT_FOUND,
                        "Account not found: " + destination));

        if (!balance.getCurrency().equals(currency)) {
            throw new PrepareTransferException(
                    FAILURE_INVALID_CURRENCY,
                    "Credit side FX is not supported");
        }

        return creditQuote
                .map(quote -> pricing.lookupQuote(quote.getId()))
                .orElseGet(() -> pricing.creditQuote(currency, balance.getCurrency()));
    }
}
