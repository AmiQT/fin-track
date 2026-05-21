import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import MyPayslips from './MyPayslips';

const mockShowToast = vi.fn();

vi.mock('../context/ToastContext', () => ({
  useToast: () => ({ showToast: mockShowToast }),
}));

vi.mock('../services/api', () => ({
  default: {
    get: vi.fn(),
  },
}));

import api from '../services/api';

const mockPayrolls = [
  {
    id: 1,
    month: 1,
    year: 2025,
    grossSalary: 5000,
    netSalary: 4200,
    totalDeductions: 800,
    epfEmployee: 550,
    epfEmployer: 650,
    socsoEmployee: 25,
    incomeTax: 225,
    unpaidLeaveDeduction: 0,
  },
  {
    id: 2,
    month: 2,
    year: 2025,
    grossSalary: 5000,
    netSalary: 4050,
    totalDeductions: 950,
    epfEmployee: 550,
    epfEmployer: 650,
    socsoEmployee: 25,
    incomeTax: 225,
    unpaidLeaveDeduction: 150,
  },
];

describe('MyPayslips', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading spinner on initial render', () => {
    api.get.mockReturnValue(new Promise(() => {}));
    render(<MyPayslips />);
    expect(document.querySelector('svg.animate-spin')).toBeInTheDocument();
  });

  it('shows empty state when no payslips exist', async () => {
    api.get.mockResolvedValue({ data: [] });

    render(<MyPayslips />);
    await waitFor(() => {
      expect(screen.getByText('No payslips yet')).toBeInTheDocument();
    });
  });

  it('renders payslip cards when data is loaded', async () => {
    api.get.mockResolvedValue({ data: mockPayrolls });

    render(<MyPayslips />);
    await waitFor(() => {
      expect(screen.getByText('January 2025')).toBeInTheDocument();
      expect(screen.getByText('February 2025')).toBeInTheDocument();
    });
  });

  it('shows net salary for each payslip', async () => {
    api.get.mockResolvedValue({ data: mockPayrolls });

    render(<MyPayslips />);
    await waitFor(() => {
      expect(screen.getByText('RM 4,200.00')).toBeInTheDocument();
      expect(screen.getByText('RM 4,050.00')).toBeInTheDocument();
    });
  });

  it('shows deduction breakdown when toggle is clicked', async () => {
    api.get.mockResolvedValue({ data: [mockPayrolls[0]] });

    const user = userEvent.setup();
    render(<MyPayslips />);
    await waitFor(() => screen.getByText('January 2025'));

    await user.click(screen.getByText(/view deduction breakdown/i));
    expect(screen.getByText('EPF (Employees Provident Fund)')).toBeInTheDocument();
    expect(screen.getByText('SOCSO (Social Security)')).toBeInTheDocument();
    expect(screen.getByText('PCB / Income Tax')).toBeInTheDocument();
  });

  it('hides deduction breakdown after second toggle click', async () => {
    api.get.mockResolvedValue({ data: [mockPayrolls[0]] });

    const user = userEvent.setup();
    render(<MyPayslips />);
    await waitFor(() => screen.getByText('January 2025'));

    await user.click(screen.getByText(/view deduction breakdown/i));
    expect(screen.getByText('EPF (Employees Provident Fund)')).toBeInTheDocument();

    await user.click(screen.getByText(/hide deduction breakdown/i));
    await waitFor(() => {
      expect(screen.queryByText('EPF (Employees Provident Fund)')).not.toBeInTheDocument();
    });
  });

  it('shows unpaid leave deduction row when value is greater than 0', async () => {
    api.get.mockResolvedValue({ data: [mockPayrolls[1]] });

    const user = userEvent.setup();
    render(<MyPayslips />);
    await waitFor(() => screen.getByText('February 2025'));

    await user.click(screen.getByText(/view deduction breakdown/i));
    expect(screen.getByText('Unpaid Leave Deduction')).toBeInTheDocument();
  });

  it('shows error toast when API call fails', async () => {
    api.get.mockRejectedValue(new Error('Network Error'));

    render(<MyPayslips />);
    await waitFor(() => {
      expect(mockShowToast).toHaveBeenCalledWith('Failed to load payslips.', 'error');
    });
  });
});
