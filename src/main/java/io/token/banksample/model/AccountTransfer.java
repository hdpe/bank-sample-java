package io.token.banksample.model;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.Double.parseDouble;

import com.google.auto.value.AutoValue;
import io.token.proto.common.account.AccountProtos.BankAccount;
import io.token.proto.common.money.MoneyProtos.Money;

/**
 * Represents a transaction posted to the source and destination accounts. The
 * change credits one account and debits the other.
 */
@AutoValue
public abstract class AccountTransfer {
    /**
     * Creates a new {@link Builder} that is used to create
     * {@link AccountTransfer} instances.
     *
     * @return new builder
     */
    public static Builder transfer() {
        return new Builder();
    }

    /**
     * Returns updatePayment id.
     *
     * @return updatePayment id
     */
    public abstract String getTransferId();

    /**
     * Returns updatePayment source/from account.
     *
     * @return from account
     */
    public abstract BankAccount getFrom();

    /**
     * Returns updatePayment destination/to account.
     *
     * @return to account
     */
    public abstract BankAccount getTo();

    /**
     * Returns updatePayment amount.
     *
     * @return updatePayment amount
     */
    public abstract double getAmount();

    /**
     * Returns updatePayment currency.
     *
     * @return currency
     */
    public abstract String getCurrency();

    /**
     * {@link AccountTransfer} builder.
     */
    public static class Builder {
        private String transferId;
        private BankAccount from;
        private BankAccount to;
        private double amount;
        private String currency;

        private Builder() {}

        /**
         * Sets unique updatePayment id.
         *
         * @param transferId updatePayment id
         * @return this object
         */
        public Builder transferId(String transferId) {
            this.transferId = transferId;
            return this;
        }

        /**
         * Sets source/from account.
         *
         * @param from from account
         * @return this object
         */
        public Builder from(BankAccount from) {
            this.from = from;
            return this;
        }

        /**
         * Sets destination/to account.
         *
         * @param to to account
         * @return this object
         */
        public Builder to(BankAccount to) {
            this.to = to;
            return this;
        }

        /**
         * Sets updatePayment amount.
         *
         * @param amount updatePayment amount
         * @param currency updatePayment currency
         * @return this object
         */
        public Builder withAmount(double amount, String currency) {
            this.amount = amount;
            this.currency = currency;
            return this;
        }

        /**
         * Sets updatePayment amount.
         *
         * @param amount updatePayment amount
         * @return this object
         */
        public Builder withAmount(Money amount) {
            this.amount = parseDouble(amount.getValue());
            this.currency = amount.getCurrency();
            return this;
        }

        /**
         * Creates new {@link AccountTransfer}.
         *
         * @return newly created {@link AccountTransfer}
         */
        public AccountTransfer build() {
            checkArgument(amount > 0, "Amount must be set");
            return new AutoValue_AccountTransfer(
                    checkNotNull(transferId, "Transfer id must be set"),
                    checkNotNull(from, "Source account must be set"),
                    checkNotNull(to, "Destination account must be set"),
                    amount,
                    checkNotNull(currency, "Currency must be set"));
        }
    }
}
