package com.example.expenseapp.utils;

import android.content.Context;
import android.os.Environment;
import android.widget.Toast;

import com.example.expenseapp.database.ExpenseRepository;
import com.example.expenseapp.models.Expense;
import com.example.expenseapp.models.Group;
import com.example.expenseapp.models.Member;
import com.example.expenseapp.models.Settlement;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class PDFGenerator {

    public static String generatePDF(Context context, String groupId) {
        ExpenseRepository repository = new ExpenseRepository(context);

        final String[] filePath = {null};
        final boolean[] isComplete = {false};

        repository.getGroupById(groupId, new ExpenseRepository.RepositoryCallback<Group>() {
            @Override
            public void onSuccess(Group group) {
                if (group == null) {
                    Toast.makeText(context, "Group not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                repository.getGroupMembers(groupId, new ExpenseRepository.RepositoryCallback<List<Member>>() {
                    @Override
                    public void onSuccess(List<Member> members) {
                        repository.getGroupExpenses(groupId, new ExpenseRepository.RepositoryCallback<List<Expense>>() {
                            @Override
                            public void onSuccess(List<Expense> expenses) {
                                repository.getGroupSettlements(groupId, new ExpenseRepository.RepositoryCallback<List<Settlement>>() {
                                    @Override
                                    public void onSuccess(List<Settlement> settlements) {
                                        filePath[0] = createPDF(context, group, members, expenses, settlements);
                                        isComplete[0] = true;
                                    }

                                    @Override
                                    public void onError(String error) {
                                        Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void onError(String error) {
                        Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(String error) {
                Toast.makeText(context, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        return filePath[0];
    }

    private static String createPDF(Context context, Group group, List<Member> members,
                                    List<Expense> expenses, List<Settlement> settlements) {
        try {
            File directory = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS), "ExpenseApp");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = "Expense_" + group.getName().replaceAll(" ", "_") +
                    "_" + System.currentTimeMillis() + ".pdf";
            File file = new File(directory, fileName);

            PdfWriter writer = new PdfWriter(new FileOutputStream(file));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // Title
            Paragraph title = new Paragraph("Expense Report: " + group.getName())
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER);
            document.add(title);

            // Group Info
            document.add(new Paragraph("Group Code: " + group.getCode()));
            document.add(new Paragraph("Created: " + formatDate(group.getCreatedDate())));
            document.add(new Paragraph("\n"));

            // Members
            document.add(new Paragraph("Members:").setBold());
            for (Member member : members) {
                document.add(new Paragraph("  • " + member.getName()));
            }
            document.add(new Paragraph("\n"));

            // Expenses
            document.add(new Paragraph("Expenses:").setBold());
            if (expenses.isEmpty()) {
                document.add(new Paragraph("No expenses yet."));
            } else {
                Table expenseTable = new Table(4);
                expenseTable.addHeaderCell("Description");
                expenseTable.addHeaderCell("Amount");
                expenseTable.addHeaderCell("Paid By");
                expenseTable.addHeaderCell("Date");

                for (Expense expense : expenses) {
                    expenseTable.addCell(expense.getDescription());
                    expenseTable.addCell(String.format(Locale.getDefault(), "₹%.2f", expense.getAmount()));

                    String paidByName = getMemberName(members, expense.getPaidByMemberId());
                    expenseTable.addCell(paidByName);

                    String formattedDate = formatExpenseDate(expense.getDate());
                    expenseTable.addCell(formattedDate);
                }
                document.add(expenseTable);
            }
            document.add(new Paragraph("\n"));

            // Calculate Total
            double totalExpense = 0;
            for (Expense expense : expenses) {
                totalExpense += expense.getAmount();
            }
            document.add(new Paragraph("Total Expenses: ₹" +
                    String.format(Locale.getDefault(), "%.2f", totalExpense)).setBold());
            document.add(new Paragraph("\n"));

            // Settlements
            document.add(new Paragraph("Settlements:").setBold());
            if (settlements.isEmpty()) {
                document.add(new Paragraph("No settlements yet."));
            } else {
                Table settlementTable = new Table(4);
                settlementTable.addHeaderCell("From");
                settlementTable.addHeaderCell("To");
                settlementTable.addHeaderCell("Amount");
                settlementTable.addHeaderCell("Status");

                for (Settlement settlement : settlements) {
                    String fromName = getMemberName(members, settlement.getFromMemberId());
                    String toName = getMemberName(members, settlement.getToMemberId());

                    settlementTable.addCell(fromName);
                    settlementTable.addCell(toName);
                    settlementTable.addCell(String.format(Locale.getDefault(), "₹%.2f", settlement.getAmount()));

                    String status = settlement.getStatus().equals("paid") ? "PAID ✓" : "PENDING";
                    settlementTable.addCell(status);
                }
                document.add(settlementTable);
            }

            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Generated on: " +
                    new SimpleDateFormat("dd MMM yyyy hh:mm a", Locale.getDefault()).format(new Date())));

            document.close();

            Toast.makeText(context, "PDF saved: " + file.getAbsolutePath(),
                    Toast.LENGTH_LONG).show();
            return file.getAbsolutePath();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error creating PDF: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private static String getMemberName(List<Member> members, String memberId) {
        for (Member member : members) {
            if (member.getId().equals(memberId)) {
                return member.getName();
            }
        }
        return "Unknown";
    }

    private static String formatDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }

    private static String formatExpenseDate(String dateStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return dateStr;
        }
    }
}