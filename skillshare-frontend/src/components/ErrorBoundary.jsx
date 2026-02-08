import React from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';

class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false, error: null, errorInfo: null };
    }

    static getDerivedStateFromError(error) {
        return { hasError: true };
    }

    componentDidCatch(error, errorInfo) {
        console.error('ErrorBoundary caught an error', error, errorInfo);
        this.setState({ error, errorInfo });
    }

    handleReset = () => {
        this.setState({ hasError: false, error: null, errorInfo: null });
        window.location.reload();
    };

    render() {
        if (this.state.hasError) {
            return (
                <div className="min-h-screen flex items-center justify-center bg-slate-50 p-4">
                    <div className="bg-white rounded-2xl shadow-xl p-8 max-w-md w-full text-center border border-slate-100">
                        <div className="w-16 h-16 bg-red-50 rounded-full flex items-center justify-center mx-auto mb-4">
                            <AlertTriangle className="w-8 h-8 text-red-500" />
                        </div>

                        <h1 className="text-2xl font-bold text-slate-900 mb-2">Something went wrong</h1>
                        <p className="text-slate-500 mb-6">
                            We encountered an unexpected error. Please try refreshing the page.
                        </p>

                        {/* Optional: Show error details in dev */}
                        {process.env.NODE_ENV === 'development' && this.state.error && (
                            <div className="text-left bg-slate-50 p-4 rounded-lg mb-6 overflow-auto max-h-40 text-xs text-slate-700 font-mono border border-slate-200">
                                {this.state.error.toString()}
                                <br />
                                {this.state.errorInfo?.componentStack}
                            </div>
                        )}

                        <button
                            onClick={this.handleReset}
                            className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-2.5 rounded-xl font-medium transition-all flex items-center justify-center gap-2 w-full"
                        >
                            <RefreshCw className="w-4 h-4" />
                            Reload Page
                        </button>
                    </div>
                </div>
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;
