import { FileQuestion, AlertCircle } from 'lucide-react';

interface EmptyStateProps {
  title: string;
  message: string;
  type?: 'empty' | 'error';
  action?: {
    label: string;
    onClick: () => void;
  };
}

export function EmptyState({ title, message, type = 'empty', action }: EmptyStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-12 px-4 sm:px-6 lg:px-8 text-center bg-white border border-gray-200 rounded-lg">
      <div className={`p-3 rounded-full mb-4 ${type === 'error' ? 'bg-danger/10 text-danger' : 'bg-muted/10 text-muted'}`}>
        {type === 'error' ? (
          <AlertCircle className="w-8 h-8" />
        ) : (
          <FileQuestion className="w-8 h-8" />
        )}
      </div>
      <h3 className="text-lg font-medium text-ink mb-2">{title}</h3>
      <p className="text-sm text-ink/70 max-w-sm mb-6">{message}</p>
      {action && (
        <button
          onClick={action.onClick}
          className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-primary-600 hover:bg-primary-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-primary-600 transition-colors"
        >
          {action.label}
        </button>
      )}
    </div>
  );
}
